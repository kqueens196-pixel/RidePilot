import collections

CUT = {0: 5, 1: 6, 2: 7, 3: 8, 4: 9, 5: 0, 6: 1, 7: 2, 8: 3, 9: 4}

# Standard 220 Patti reference
PANELS_BANK = {
    0: ["145", "235", "190", "280", "460", "550", "127", "136"],
    1: ["128", "146", "245", "380", "155", "678", "236", "290"],
    2: ["147", "237", "345", "480", "255", "129", "156", "246"],
    3: ["148", "256", "157", "689", "355", "139", "120", "166"],
    4: ["149", "239", "248", "347", "455", "789", "130", "158"],
    5: ["140", "230", "159", "258", "555", "348", "267", "357"],
    6: ["150", "240", "268", "358", "556", "169", "259", "349"],
    7: ["124", "340", "250", "278", "566", "179", "160", "269"],
    8: ["170", "260", "350", "468", "440", "189", "279", "369"],
    9: ["199", "180", "270", "450", "478", "289", "144", "379"]
}

# Pichle 2 hafton ka complete record (Open Panel, Jodi, Close Panel)
# Including latest: 199 | 92 | 390
TWO_WEEKS_DATA = [
    # Week 1
    ("458", "77", "679"), ("469", "31", "789"), ("319", "48", "780"),
    ("178", "32", "478"), ("147", "08", "780"), ("558", "60", "389"), ("378", "52", "246"),
    # Week 2
    ("690", "50", "450"), ("458", "26", "178"), ("426", "44", "240"),
    ("314", "52", "490"), ("419", "61", "990"), ("556", "64", "239"), 
    ("199", "92", "390") # Latest result
]

def analyze_two_weeks(data):
    open_anks = []
    close_anks = []
    jodi_totals = []
    jodi_diffs = []
    
    for op, jd, cp in data:
        oa = sum(int(d) for d in op) % 10
        ca = sum(int(d) for d in cp) % 10
        open_anks.append(oa)
        close_anks.append(ca)
        jodi_totals.append((oa + ca) % 10)
        jodi_diffs.append(abs(oa - ca))
        
    # --- LOGIC 1: Weekly Day Cross-Pattern ---
    # Pichle hafte ke same day ka result (7 days back)
    same_day_last_week = data[-7]
    prev_week_oa = sum(int(d) for d in same_day_last_week[0]) % 10
    prev_week_ca = sum(int(d) for d in same_day_last_week[2]) % 10
    
    # --- LOGIC 2: Due / Pending Numbers Analysis ---
    all_seen = set(open_anks[-10:] + close_anks[-10:])
    due_anks = [d for d in range(10) if d not in all_seen]
    
    # --- LOGIC 3: Momentum & Bracket Total Frequency ---
    total_freq = collections.Counter(jodi_totals)
    most_common_total = total_freq.most_common(1)[0][0]
    
    # Latest game: 199-92-390
    last_oa = open_anks[-1]
    last_ca = close_anks[-1]

    # Combine Cross Logic:
    # Target 1: Previous week cross family ank
    t1 = prev_week_oa
    # Target 2: Cut of previous week cross
    t2 = CUT[t1]
    # Target 3: Touch from latest result (due ank ya balancing diff)
    t3 = CUT[last_oa]
    t4 = (last_ca + 1) % 10

    candidates = [t1, t2, t3, t4]
    if len(set(candidates)) < 4:
        for extra in due_anks:
            if extra not in candidates:
                candidates.append(extra)
            if len(set(candidates)) == 4:
                break
                
    final_otc = sorted(list(set(candidates)))[:4]

    # 10 Panels filter across the 4 final OTC
    top_10_panels = []
    for ank in final_otc:
        bank = PANELS_BANK.get(ank, [])
        for p in bank[:3]:
            if (ank, p) not in top_10_panels:
                top_10_panels.append((ank, p))
            if len(top_10_panels) == 10:
                break
        if len(top_10_panels) == 10:
            break

    # Format Display
    print("\n" + "="*65)
    print("      SRIDEVI NIGHT: 2-WEEK HISTORICAL PATTERN ENGINE")
    print("="*65)
    print(f"Total History Parsed   : {len(data)} Days (Full 2 Weeks)")
    print(f"Latest Result          : {data[-1][0]} | {data[-1][1]} | {data[-1][2]}")
    print(f"Same Day Last Week Ref : {same_day_last_week[0]} | {same_day_last_week[1]} | {same_day_last_week[2]}")
    print("-" * 65)
    print(f"Cross Analysis Trick   : Weekly Anchor={t1} (Cut {t2}) | Line Anchor={t3}, {t4}")
    print(f"NEXT DAY STRONG 4 OTC  : {final_otc}")
    print("-" * 65)

    print("TOP 10 HIGH-CHANCE PANELS (FILTERED):")
    for idx, (ank, p) in enumerate(top_10_panels, 1):
        print(f"  {idx:02d}. Ank [{ank}] -> Panel: {p}")

    print("-" * 65)
    print("SELECTED 2-WEEK CROSS JODIS:")
    jodis = []
    for o in final_otc[:2]:
        for c in final_otc[2:]:
            jodis.append(f"{o}{c}")
            jodis.append(f"{c}{o}")
    print("  " + "   ".join(jodis))

    print("-" * 65)
    print("SELECTED FULL SANGAM (TOP 4):")
    for i in range(4):
        print(f"  Sangam {i+1}: {top_10_panels[i][1]} x {top_10_panels[-(i+1)][1]}")
    print("="*65 + "\n")

if __name__ == "__main__":
    analyze_two_weeks(TWO_WEEKS_DATA)
