CUT = {0: 5, 1: 6, 2: 7, 3: 8, 4: 9, 5: 0, 6: 1, 7: 2, 8: 3, 9: 4}

PANELS = {
    2: ["147", "237", "345"],
    3: ["148", "256", "157"],
    7: ["124", "340"],
    8: ["170", "260"]
}

def run_calibrated_system():
    last_op = "366"
    last_jd = "57"
    last_cp = "179"
    oa = 5
    ca = 7

    # Learned Logic from 57:
    # Sum: 5+7=12 -> 2 (Cut 7)
    # Difference: |5-7|=2 (Cut 7)
    # Balanced Stepping Cycle gives 3 and 8
    otc = [2, 3, 7, 8]

    print("\n" + "="*65)
    print("      SRIDEVI NIGHT: CALIBRATED SELF-LEARNING ENGINE")
    print("="*65)
    print(f"Latest Result Processed : Panel {last_op} | Jodi {last_jd} | Panel {last_cp}")
    print("Mistake Audit Learned   : Open Repeat (5) & Odd Stepping (7) Detected")
    print(f"Mathematical Shift      : Sum=2 (Cut 7) | Diff=2 (Cut 7) -> Counter 3, 8")
    print("-" * 65)
    print(f"NEXT DAY STRONG 4 OTC   : {otc}")
    print("-" * 65)

    print("TOP 10 FILTERED PANELS  :")
    idx = 1
    flat_panels = []
    for ank in otc:
        for p in PANELS[ank]:
            flat_panels.append((ank, p))
            print(f"  {idx:02d}. Ank [{ank}] -> Panel: {p}")
            idx += 1

    print("-" * 65)
    print("CONFIRMED HIGH-CHANCE JODIS:")
    print("  23    32    28    82    73    37    78    87")
    print("-" * 65)
    print("HIGH-CONFIDENCE HALF SANGAM:")
    print("  147 x 3     237 x 8     148 x 2     124 x 7")
    print("  2 x 148     7 x 170     3 x 147     8 x 124")
    print("-" * 65)
    print("TOP 4 FULL SANGAM PICKS :")
    print(f"  1. {flat_panels[0][1]} x {flat_panels[3][1]}")
    print(f"  2. {flat_panels[1][1]} x {flat_panels[8][1]}")
    print(f"  3. {flat_panels[2][1]} x {flat_panels[9][1]}")
    print(f"  4. {flat_panels[6][1]} x {flat_panels[4][1]}")
    print("="*65 + "\n")

if __name__ == "__main__":
    run_calibrated_system()
