# Master Pre-Market Matrix Engine for Sridevi Night

CUT = {0: 5, 1: 6, 2: 7, 3: 8, 4: 9, 5: 0, 6: 1, 7: 2, 8: 3, 9: 4}

# High-frequency calibrated panels (Single + Double family mix)
PANEL_MATRIX = {
    0: ["190", "145", "280", "550"],
    4: ["149", "239", "347", "789"],
    5: ["140", "230", "159", "555"],
    9: ["180", "270", "199", "450"]
}

def generate_pre_market_matrix():
    # Base Anchor: [0, 4, 5, 9] from 32 & 57 failure-loop learning
    otc = [0, 4, 5, 9]

    # Primary Locked Panels (1 best per OTC for Sangam mapping)
    p_0 = PANEL_MATRIX[0][0]  # 190
    p_4 = PANEL_MATRIX[4][0]  # 149
    p_5 = PANEL_MATRIX[5][0]  # 140
    p_9 = PANEL_MATRIX[9][0]  # 180

    print("\n" + "="*65)
    print("   SRIDEVI NIGHT: PRE-MARKET LOCKED SANGAM & OTC MATRIX")
    print("   (Yeh game market open hone se pehle freeze karna hai)")
    print("="*65)
    
    print(f"LOCKED 4 OTC (OPEN TO CLOSE) : {otc}")
    print("-" * 65)

    print("TOP 4 CALIBRATED PANELS      :")
    print(f"  Ank [0] -> Panel: {p_0}")
    print(f"  Ank [4] -> Panel: {p_4}")
    print(f"  Ank [5] -> Panel: {p_5}")
    print(f"  Ank [9] -> Panel: {p_9}")
    print("-" * 65)

    print("LOCKED HIGH-PROBABILITY JODIS:")
    print("  Primary: 04    59")
    print("  Cut Support: 40    95")
    print("-" * 65)

    print("PRE-MARKET HALF SANGAM (TOP 4):")
    print(f"  1. [Type A] {p_4} x 0   (Open Panel x Close Ank)")
    print(f"  2. [Type A] {p_9} x 5   (Open Panel x Close Ank)")
    print(f"  3. [Type B] 0 x {p_4}   (Open Ank x Close Panel)")
    print(f"  4. [Type B] 5 x {p_9}   (Open Ank x Close Panel)")
    print("-" * 65)

    print("PRE-MARKET FULL SANGAM (TOP 4):")
    print(f"  Sangam 1: {p_4} x {p_0}   (4 x 0 -> Jodi 40)")
    print(f"  Sangam 2: {p_0} x {p_4}   (0 x 4 -> Jodi 04)")
    print(f"  Sangam 3: {p_9} x {p_5}   (9 x 5 -> Jodi 95)")
    print(f"  Sangam 4: {p_5} x {p_9}   (5 x 9 -> Jodi 59)")
    print("="*65 + "\n")

if __name__ == "__main__":
    generate_pre_market_matrix()
