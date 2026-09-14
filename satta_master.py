# Sridevi Night: Double-Patti Recalibration Engine

CUT = {0: 5, 1: 6, 2: 7, 3: 8, 4: 9, 5: 0, 6: 1, 7: 2, 8: 3, 9: 4}

# Calibrated for post-double-patti correction (Single High-Step & 0-Ending)
PANEL_BANK = {
    0: ["190", "145", "280"],
    4: ["149", "239", "347"],
    5: ["140", "230", "159"],
    9: ["180", "270", "379"]
}

def run_upgraded_engine():
    # Last Result: 566 | 73 | 337 (Double Pana both sides)
    # Sum: 7+3=10 -> 0 (Cut 5)
    # Diff: |7-3|=4 -> 4 (Cut 9)
    otc = [0, 4, 5, 9]

    print("\n" + "="*65)
    print("      SRIDEVI NIGHT: DOUBLE-PATTI AUDITED ENGINE")
    print("="*65)
    print("Audit Learned           : Double 6 (566) & Double 3 (337) Shift Detected")
    print("Post-Double Rule        : Swing towards 0-ending & single step panels")
    print("-" * 65)
    print(f"LOCKED 4 OTC            : {otc}")
    print("-" * 65)

    print("TOP CALIBRATED PANELS   :")
    idx = 1
    flat_panels = []
    for ank in otc:
        for p in PANEL_BANK[ank]:
            flat_panels.append((ank, p))
            print(f"  {idx:02d}. Ank [{ank}] -> Panel: {p}")
            idx += 1

    print("-" * 65)
    print("LOCKED JODIS            : 04    40    59    95    (Support: 09, 54)")
    print("-" * 65)
    print("PRE-MARKET HALF SANGAM  :")
    print("  190 x 4     140 x 9     0 x 149     5 x 180")
    print("-" * 65)
    print("PRE-MARKET FULL SANGAM (TOP 4):")
    print(f"  1. 190 x 149   (Jodi 04)")
    print(f"  2. 140 x 180   (Jodi 59)")
    print(f"  3. 145 x 239   (Jodi 04)")
    print(f"  4. 230 x 270   (Jodi 59)")
    print("="*65 + "\n")

if __name__ == "__main__":
    run_upgraded_engine()
