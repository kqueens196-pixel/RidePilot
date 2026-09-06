import time
import re
import requests
from bs4 import BeautifulSoup
import datetime

CHART_URL = "https://sattamatkajodi.net/record/sridevi-night-satta-penal-chart.php#bottom"
CUT = {0: 5, 1: 6, 2: 7, 3: 8, 4: 9, 5: 0, 6: 1, 7: 2, 8: 3, 9: 4}

PANELS = {
    0: ["145", "235"], 1: ["128", "146"], 2: ["147", "237"],
    3: ["148", "256"], 4: ["149", "239"], 5: ["140", "230"],
    6: ["150", "240"], 7: ["124", "340"], 8: ["170", "260"],
    9: ["199", "180"]
}

def get_current_live():
    headers = {"User-Agent": "Mozilla/5.0 (Linux; Android 12; Mobile)"}
    try:
        resp = requests.get(CHART_URL, headers=headers, timeout=10)
        if resp.status_code == 200:
            soup = BeautifulSoup(resp.text, "html.parser")
            text = soup.get_text()
            
            # 1. Check Completed Result (OpenPanel Jodi ClosePanel)
            completed = re.findall(r'(\b\d{3}\b)\s*[-|]?\s*(\b\d{2}\b)\s*[-|]?\s*(\b\d{3}\b)', text)
            if completed:
                last_c = completed[-1]
                oa = sum(int(d) for d in last_c[0]) % 10
                ca = sum(int(d) for d in last_c[2]) % 10
                if f"{oa}{ca}" == last_c[1]:
                    return "COMPLETED", last_c[0], last_c[1], last_c[2]
            
            # 2. Check Only Open Live (e.g. 199-9)
            live_open = re.findall(r'(\b\d{3}\b)\s*[-*]\s*(\b\d{1}\b)', text)
            if live_open:
                last_o = live_open[-1]
                oa = sum(int(d) for d in last_o[0]) % 10
                if str(oa) == last_o[1]:
                    return "OPEN_ONLY", last_o[0], last_o[1], None
    except Exception:
        pass
    return None, None, None, None

def calculate_next(oa, ca):
    total = (oa + ca) % 10
    diff = abs(oa - ca)
    otc = sorted(list(set([total, CUT[total], diff, CUT[diff]])))
    
    print("\n" + "="*58)
    print(f"   [AUTOMATIC RESULT DETECTED] -> Anks: Open={oa} | Close={ca}")
    print("="*58)
    print(f"Formula Output        : Sum={total} (Cut {CUT[total]}) | Diff={diff} (Cut {CUT[diff]})")
    print(f"NEXT ROUND 4 OTC ANKS : {otc}")
    print("-" * 58)
    print("TOP PANELS:")
    for ank in otc:
        print(f"  Ank [{ank}] -> " + ", ".join(PANELS.get(ank, [])))
    print("="*58 + "\n")

def start_auto_monitor():
    print("="*58)
    print("   SRIDEVI NIGHT: 24/7 AUTO LIVE RESULT DETECTOR")
    print("="*58)
    print(f"Target URL: {CHART_URL}")
    print("[*] Engine background me check kar raha hai...")
    
    last_seen_state = None

    while True:
        status, p1, mid, p2 = get_current_live()
        current_signature = f"{status}_{p1}_{mid}_{p2}"

        if status and current_signature != last_seen_state:
            now = datetime.datetime.now().strftime("%H:%M:%S")
            print(f"\n[{now}] Naya Update Aaya: {p1} | {mid} | {p2 if p2 else '***'}")
            
            if status == "COMPLETED":
                oa = sum(int(d) for d in p1) % 10
                ca = sum(int(d) for d in p2) % 10
                calculate_next(oa, ca)
            elif status == "OPEN_ONLY":
                oa = int(mid)
                print(f"-> Open Panel {p1} Pass! Close Pending. Close OTC Targets: {sorted([CUT[oa], (oa+1)%10, (oa-1)%10])}")
            
            last_seen_state = current_signature
        
        # Har 40 seconds me check karega
        time.sleep(40)

if __name__ == "__main__":
    start_auto_monitor()
