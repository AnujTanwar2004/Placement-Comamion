"""Record a real emulator demo; locate controls from UI XML before each action."""
from pathlib import Path
import os, subprocess, time, re, xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parent
ADB=Path(os.environ['LOCALAPPDATA'])/'Android/Sdk/platform-tools/adb.exe'
def adb(*args):
    return subprocess.run([str(ADB),'-s','emulator-5554',*args],capture_output=True,check=True).stdout.decode(errors='replace')
def ui():
    adb('shell','uiautomator','dump','/sdcard/placement-record-ui.xml')
    return ET.fromstring(adb('shell','cat','/sdcard/placement-record-ui.xml'))
def tap_node(n):
    x1,y1,x2,y2=map(int,re.findall(r'\d+',n.attrib['bounds']))
    adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2)); time.sleep(1.2)
def click(label):
    node=next((n for n in ui().iter('node') if n.get('text')==label or n.get('content-desc')==label),None)
    if node is None: raise RuntimeError('Control not visible: '+label)
    tap_node(node)
def fill(index,value,old_length=0):
    edits=[n for n in ui().iter('node') if n.get('class')=='android.widget.EditText']
    tap_node(edits[index])
    if old_length:
        adb('shell','input','keyevent','123')
        adb('shell','input','keyevent',*(['67']*old_length))
    adb('shell','input','text',value.replace(' ','%s')); time.sleep(1.2)
    adb('shell','input','keyevent','4'); time.sleep(1.4)
def shot(name):
    adb('shell','screencap','-p','/sdcard/placement-record.png')
    adb('pull','/sdcard/placement-record.png',str(ROOT/'assets'/f'{name}.png'))

adb('shell','am','force-stop','com.example.placementcompanion')
adb('shell','am','start','-W','-n','com.example.placementcompanion/.MainActivity'); time.sleep(2)
record=subprocess.Popen([str(ADB),'-s','emulator-5554','shell','screenrecord','--time-limit','180','--bit-rate','4500000','--size','720x1600','/sdcard/placement-final-demo.mp4'],stdout=subprocess.DEVNULL,stderr=subprocess.PIPE)
events=[]; began=time.monotonic()
def event(label):
    events.append((round(time.monotonic()-began),label)); print(label,flush=True)
try:
    time.sleep(3); event('Home dashboard and bottom navigation')
    click('Planner'); event('Study planner and sample tasks')
    click('Add Task'); click('Add Task'); event('Blank task title is guarded')
    fill(0,'Arrays Revision - Lab Demo')
    fill(3,'2026-10-05',10)
    click('Add Task'); time.sleep(2); event('New study task saved in Room')
    shot('planner-demo')
    # Locate the newly added task and its checkbox within the same parent card.
    tree=ui(); parents={child:parent for parent in tree.iter() for child in parent}
    task=next(n for n in tree.iter('node') if n.get('text')=='Arrays Revision - Lab Demo')
    ancestor=task
    while ancestor in parents:
        ancestor=parents[ancestor]
        checkbox=next((n for n in ancestor.iter('node') if n.get('checkable')=='true'),None)
        if checkbox is not None: tap_node(checkbox); break
    event('Toggle task completion')
    click('Home'); time.sleep(2); event('Home returns to the dashboard')
    adb('shell','am','force-stop','com.example.placementcompanion')
    adb('shell','am','start','-W','-n','com.example.placementcompanion/.MainActivity'); time.sleep(2)
    click('Planner'); event('Relaunch: saved task is retained')
    assert any(n.get('text')=='Arrays Revision - Lab Demo' for n in ui().iter('node'))
    click('AI Plan Mode'); fill(1,'2',2); click('Generate AI Study Plan')
    event('Real Groq API request: two-day study plan')
    for _ in range(8):
        time.sleep(2)
        if any('Generated Roadmap' in n.get('text','') for n in ui().iter('node')): break
    else: raise RuntimeError('AI roadmap did not appear')
    adb('shell','input','swipe','540','1850','540','650','900'); time.sleep(2)
    shot('ai-output'); event('Generated roadmap and study tasks')
    # Save button may require scrolling slightly upward after reviewing output.
    try: click('Save to Planner')
    except RuntimeError:
        adb('shell','input','swipe','540','700','540','1700','800'); time.sleep(1)
        click('Save to Planner')
    time.sleep(2); event('AI tasks saved to the local planner')
    click('Companies'); time.sleep(3); event('Company directory and filtering')
    click('Home'); time.sleep(4); event('Final dashboard')
finally:
    pid=adb('shell','pidof','screenrecord').strip()
    if pid: adb('shell','kill','-2',*pid.split())
    record.wait(timeout=20)
    adb('pull','/sdcard/placement-final-demo.mp4',str(ROOT/'Placement_Companion_Demo_Backup.mp4'))
    (ROOT/'Demo_Video_Cues.txt').write_text('\n'.join(f'{t//60:02d}:{t%60:02d}  {label}' for t,label in events),encoding='utf-8')
    print(f'Recorded {round(time.monotonic()-began)} seconds.',flush=True)
