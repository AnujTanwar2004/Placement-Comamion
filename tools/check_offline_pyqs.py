"""Exercise real app search with emulator Wi-Fi and mobile data disabled."""
from pathlib import Path
import os, re, subprocess, time, xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parent.parent
ADB=Path(os.environ['LOCALAPPDATA'])/'Android/Sdk/platform-tools/adb.exe'
def adb(*args):
    return subprocess.run([str(ADB),'-s','emulator-5554',*args],capture_output=True,check=True).stdout.decode(errors='replace').strip()
def ui():
    adb('shell','uiautomator','dump','/sdcard/offline-pyq-ui.xml')
    return ET.fromstring(adb('shell','cat','/sdcard/offline-pyq-ui.xml'))
def click(label):
    n=next(n for n in ui().iter('node') if n.get('text')==label or n.get('content-desc')==label)
    x1,y1,x2,y2=map(int,re.findall(r'\d+',n.get('bounds')))
    adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2)); time.sleep(1.5)
def screenshot(name):
    adb('shell','screencap','-p','/sdcard/offline-pyq.png')
    adb('pull','/sdcard/offline-pyq.png',str(ROOT/'data/leetcode-companywise'/name))

wifi=adb('shell','settings','get','global','wifi_on')
mobile=adb('shell','settings','get','global','mobile_data')
try:
    adb('shell','svc','wifi','disable'); adb('shell','svc','data','disable')
    adb('shell','am','force-stop','com.example.placementcompanion')
    adb('shell','am','start','-W','-n','com.example.placementcompanion/.MainActivity'); time.sleep(4)
    click('Search & Saved')
    n=next(n for n in ui().iter('node') if n.get('class')=='android.widget.EditText')
    x1,y1,x2,y2=map(int,re.findall(r'\d+',n.get('bounds')))
    adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2)); time.sleep(1)
    adb('shell','input','text','NVIDIA'); time.sleep(2)
    adb('shell','input','keyevent','4'); time.sleep(1)
    click('Browse all matching company questions'); time.sleep(2)
    labels=[n.get('text','') for n in ui().iter('node')]
    assert '139 matching questions · Showing 100' in labels, labels
    screenshot('offline-nvidia-search.png')
    click('Filters'); click('30 days'); time.sleep(2)
    labels=[n.get('text','') for n in ui().iter('node')]
    assert '1 matching questions · Showing 1' in labels, labels
    assert 'Open in LeetCode' in labels
    screenshot('offline-nvidia-recent.png')
    print('PASS: Wi-Fi/mobile data disabled; global NVIDIA search found 139 questions; 30-day filter found 1; LeetCode action displayed.')
finally:
    adb('shell','svc','wifi','enable' if wifi=='1' else 'disable')
    adb('shell','svc','data','enable' if mobile=='1' else 'disable')
