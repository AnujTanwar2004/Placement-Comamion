"""Convert every downloaded company CSV into a validated offline Room seed asset.

Uses only CSV data and README; does not execute the upstream scraper.
Run from the project root after downloading data/leetcode-companywise/source.zip.
"""
from pathlib import Path
import csv, gzip, hashlib, io, json, re, zipfile

ROOT = Path(__file__).resolve().parent.parent
DATA = ROOT / 'data' / 'leetcode-companywise'
ASSETS = ROOT / 'app' / 'src' / 'main' / 'assets' / 'company_questions'
WINDOWS = {'all.csv': 'frequencyAll', 'thirty-days.csv': 'frequency30Days',
           'three-months.csv': 'frequency3Months', 'six-months.csv': 'frequency6Months',
           'more-than-six-months.csv': 'frequencyOlder'}
NAMES = {'amazon':'Amazon', 'microsoft':'Microsoft', 'google':'Google', 'tcs':'TCS',
         'infosys':'Infosys', 'facebook':'Meta (Facebook)', 'meta':'Meta (Facebook)',
         'ibm':'IBM', 'jpmorgan':'JPMorgan', 'jpmorgan-chase':'JPMorgan Chase',
         'paypal':'PayPal', 'bytedance':'ByteDance', 'linkedin':'LinkedIn',
         'leetcode':'LeetCode', 'uber':'Uber', 'nvidia':'NVIDIA', 'amd':'AMD',
         'at-t':'AT&T', 'att':'AT&T', '6sense':'6sense', '1kosmos':'1Kosmos'}
KNOWN_IDS = {'amazon':1, 'microsoft':2, 'google':3, 'tcs':4, 'infosys':5}

def stable_id(key):
    return -(int.from_bytes(hashlib.sha256(key.encode()).digest()[:8], 'big') & ((1 << 63)-1))

def percent(value):
    value=value.strip().rstrip('%')
    if not value: return None
    n=float(value)
    if not 0 <= n <= 100: raise ValueError(f'Invalid percentage: {value}')
    return n

def prepare():
    ASSETS.mkdir(parents=True, exist_ok=True)
    (DATA/'csv').mkdir(parents=True, exist_ok=True)
    commit=json.loads((DATA/'commit.json').read_text(encoding='utf-8-sig'))['sha']
    questions={}; csv_count=0; raw_rows=0; companies=set()
    with zipfile.ZipFile(DATA/'source.zip') as archive:
        readme=archive.read(next(n for n in archive.namelist() if n.endswith('/README.md'))).decode()
        snapshot='2026-07-12'
        for path in sorted(archive.namelist()):
            parts=path.split('/')
            if len(parts)!=3 or not path.endswith('.csv'): continue
            slug,filename=parts[-2:]
            if filename not in WINDOWS: raise ValueError(f'Unrecognized time window: {path}')
            raw=archive.read(path)
            local=DATA/'csv'/slug/filename; local.parent.mkdir(exist_ok=True)
            local.write_bytes(raw)
            csv_count+=1; companies.add(slug)
            name=NAMES.get(slug,slug.replace('-',' ').title())
            rows=csv.DictReader(io.StringIO(raw.decode('utf-8-sig')))
            assert set(rows.fieldnames)=={'ID','URL','Title','Difficulty','Acceptance %','Frequency %'}
            for row in rows:
                raw_rows+=1; leetcode_id=int(row['ID'])
                url=row['URL'].strip()
                assert re.fullmatch(r'https://leetcode\.com/problems/[a-z0-9-]+/?',url),url
                assert row['Difficulty'] in ('Easy','Medium','Hard')
                key=(slug,leetcode_id)
                if key not in questions:
                    questions[key]={
                        'id':stable_id(f'{slug}:{leetcode_id}'),
                        'companyId':KNOWN_IDS.get(slug,abs(stable_id(slug))%1_000_000_000+10000),
                        'companyName':name,'companySlug':slug,'leetcodeId':leetcode_id,
                        'title':row['Title'].strip(), 'description':'Company-associated coding practice. Open LeetCode for the problem statement.',
                        'topic':'Coding','difficulty':row['Difficulty'],'year':0,
                        'round':'Not specified','role':'Not specified',
                        'source':'LeetCode company-wise community dataset', 'solutionLink':url,
                        'tags':'', 'isSolved':False, 'isBookmarked':False, 'userNote':'',
                        'snapshotDate':snapshot, 'acceptanceRate':percent(row['Acceptance %']),
                        **{column:None for column in WINDOWS.values()}
                    }
                q=questions[key]
                assert q['solutionLink'].rstrip('/')==url.rstrip('/'),key
                # A listed row with an empty frequency still belongs to its time window.
                q[WINDOWS[filename]]=percent(row['Frequency %']) or 0.0
                if filename=='all.csv': q['acceptanceRate']=percent(row['Acceptance %'])
        (DATA/'UPSTREAM_README.md').write_text(readme,encoding='utf-8')
    records=sorted(questions.values(),key=lambda q:(q['companySlug'],q['leetcodeId']))
    assert len({q['id'] for q in records})==len(records),'Stable ID collision'
    with gzip.GzipFile(filename=str(ASSETS/'questions.json.gz'),mode='wb',mtime=0) as f:
        f.write(json.dumps(records,ensure_ascii=False,separators=(',',':')).encode())
    manifest={'repository':'https://github.com/snehasishroy/leetcode-companywise-interview-questions',
              'commit':commit,'snapshotDate':snapshot,'companyCount':len(companies),
              'csvCount':csv_count,'rawRows':raw_rows,'questionCount':len(records),
              'assetSha256':hashlib.sha256((ASSETS/'questions.json.gz').read_bytes()).hexdigest()}
    for target in [ASSETS/'manifest.json',DATA/'manifest.json']:
        target.write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(manifest,indent=2))

if __name__=='__main__': prepare()
