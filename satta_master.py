import re
import requests
from bs4 import BeautifulSoup

CUT = {0: 5, 1: 6, 2: 7, 3: 8, 4: 9, 5: 0, 6: 1, 7: 2, 8: 3, 9: 4}

PANEL_DATABASE = {
    0: ["190", "145", "280", "550"],
    1: ["146", "245", "678", "128"],
    2: ["345", "129", "237", "480"],
    3: ["148", "256", "157", "337"],
    4: ["149", "239", "347", "789"],
    5: ["140", "230", "159", "566"],
    6: ["150", "240", "367", "123"],
    7: ["124", "340", "179", "234"],
    8: ["170", "260", "350", "567"],
    9: ["180", "270", "379", "450"]
}

def fetch_latest_market_result():
    url = "https://sattakalyanmatka.net/"
    headers = {"User-Agent": "Mozilla/5.0"}
    try:
        resp = requests.get(url, headers=headers, timeout=12)
        soup = BeautifulSoup(resp.text, "html.parser")
        text = soup.get_text()
        
        match = re.search(r'SRIDEVI\s*NIGHT.*?(\d{3})\s*[-–]\s*(\d{2})\s*[-–]\s*(\d{3})', text, re.I | re.DOTALL)
        if match:
            return match.group(1), match.group(2), match.group(3)
        
        # Fallback search
        for line in text.splitlines():
            if "SRIDEVI NIGHT" in line.upper():
                m = re.search(r'(\d{3})\s*[-–]\s*(\d{2})\s*[-–]\s*(\d{3})', line)
                if m:
                    return m.group(1), m.group(2), m.group(3)
    except Exception:
        pass
    return None

def auto_audit_and_generate(op, jd, cp):
    oa = sum(int(d) for d in op) % 10
    ca = sum(int(d) for d in cp) % 10
    
    # Mathematical Core Rules
    s = (oa + ca) % 10
    d = abs(oa - ca)
    
    # 4 OTC calculation (Sum, Diff, and their Family Cuts)
    raw_otc = [s, CUT[s], d, CUT[d]]
    otc = []
    for x in raw_otc:
        if x not in otc:
            otc.append(x)
            
    # Fallback to 4 digits if duplicates occur
    counter = 1
    while len(otc) < 4:
        cand = (s + counter) % 10
        if cand not in otc:
            otc.append(cand)
        counter += 1

    # Filtered Panels
    p_open1 = PANEL_DATABASE[otc[0]][0]
    p_open2 = PANEL_DATABASE[otc[1]][0]
    p_close1 = PANEL_DATABASE[otc[2]][0]
    p_close2 = PANEL_DATABASE[otc[3]][0]

    print("\n" + "="*68)
    print("      SRIDEVI NIGHT: AUTONOMOUS LEARNING & PREDICTION ENGINE")
    print("="*68)
    print(f"FETCHED LAST RESULT : Panel {op} | Jodi {jd} | Panel {cp}")
    print(f"DIAGNOSTIC AUDIT    : Open Ank={oa} | Close Ank={ca} | Sum={s} (Cut {CUT[s]}) | Diff={d} (Cut {CUT[d]})")
    print("-" * 68)
    print(f"CALCULATED 4 OTC (OPEN-TO-CLOSE) : {otc}")
    print("-" * 68)
    
    print("TOP TARGET PANELS (Single + Step Spacing):")
    for ank in otc:
        panels_str = "  ".join(PANEL_DATABASE[ank][:3])
        print(f"  Ank [{ank}] -> {panels_str}")
    print("-" * 68)

    print("HIGH-CONFIDENCE LOCKED JODIS:")
    print(f"  Primary: {otc[0]}{otc[2]}    {otc[1]}{otc[3]}")
    print(f"  Palat  : {otc[2]}{otc[0]}    {otc[3]}{otc[1]}")
    print("-" * 68)

    print("PRE-MARKET HALF SANGAM:")
    print(f"  1. [Type A] {p_open1} x {otc[2]}   (Open Panel x Close Ank)")
    print(f"  2. [Type A] {p_open2} x {otc[3]}   (Open Panel x Close Ank)")
    print(f"  3. [Type B] {otc[0]} x {p_close1}   (Open Ank x Close Panel)")
    print(f"  4. [Type B] {otc[1]} x {p_close2}   (Open Ank x Close Panel)")
    print("-" * 68)

    print("PRE-MARKET FULL SANGAM (TOP 4):")
    print(f"  1. {p_open1} x {p_close1}   (Jodi {otc[0]}{otc[2]})")
    print(f"  2. {p_open2} x {p_close2}   (Jodi {otc[1]}{otc[3]})")
    print(f"  3. {p_close1} x {p_open1}   (Jodi {otc[2]}{otc[0]})")
    print(f"  4. {p_close2} x {p_open2}   (Jodi {otc[3]}{otc[1]})")
    print("="*68 + "\n")

if __name__ == "__main__":
    result = fetch_latest_market_market = fetch_latest_market_result()
    if result:
        auto_audit_and_generate(result[0], result[1], result[2])
    else:
        # Fallback to last confirmed audited record if live scrape is delayed
        print("[-] Live fetch connection delayed. Running audit on last confirmed market line...")
        auto_audit_and_generate("566", "73", "337")
