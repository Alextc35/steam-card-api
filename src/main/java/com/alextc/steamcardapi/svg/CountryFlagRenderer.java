package com.alextc.steamcardapi.svg;

import java.util.Locale;
import java.util.Set;

final class CountryFlagRenderer {

    static final int WIDTH = 26;
    static final int HEIGHT = 18;
    static final Set<String> SUPPORTED_COUNTRY_CODES = Set.of(
            "CN", "US", "RU", "BR", "DE", "GB", "TR", "PL", "FR", "CA",
            "JP", "KR", "IN", "ES", "IT", "UA", "AU", "MX", "ID", "VN",
            "AR", "NL", "SE", "FI", "NO", "CZ", "RO", "TW", "TH", "PH",
            "MY", "HU", "BE", "AT", "PT", "DK", "CH", "CL", "GR", "NZ",
            "IE", "SG", "RS", "HR", "SK", "BG", "KZ", "BY", "ZA", "SA",
            "AE", "IL", "EG", "PK", "BD", "LK", "NP", "MA", "DZ", "TN",
            "PE", "CO", "VE", "EC", "UY", "PY", "BO", "CR", "PA", "GT",
            "DO", "HN", "SV", "NI", "LU", "SI", "LT", "LV", "EE", "IS",
            "GE", "AM", "AZ", "UZ", "KG", "MN", "HK", "MO", "NG", "KE",
            "GH", "ET", "QA", "KW", "BH", "OM", "JO", "LB", "CY", "MT");

    private CountryFlagRenderer() {
    }

    static String render(String countryCode, SvgTheme.Palette palette, int x, int y) {
        String code = normalize(countryCode);
        String body = switch (code) {
            case "CN" -> china();
            case "US" -> unitedStates();
            case "RU" -> h("#ffffff", "#0039a6", "#d52b1e");
            case "BR" -> brazil();
            case "DE" -> h("#000000", "#dd0000", "#ffce00");
            case "GB" -> unionJack();
            case "TR" -> turkey();
            case "PL" -> h("#ffffff", "#dc143c");
            case "FR" -> v("#0055a4", "#ffffff", "#ef4135");
            case "CA" -> canada();
            case "JP" -> full("#ffffff") + circle(11, 7, 4.2, "#bc002d");
            case "KR" -> southKorea();
            case "IN" -> india();
            case "ES" -> h(new double[] {2, 4, 2}, "#aa151b", "#f1bf00", "#aa151b");
            case "IT" -> v("#009246", "#ffffff", "#ce2b37");
            case "UA" -> h("#0057b7", "#ffd700");
            case "AU" -> australia();
            case "MX" -> mexico();
            case "ID" -> h("#ce1126", "#ffffff");
            case "VN" -> full("#da251d") + star(11, 7, 4.1, 1.6, "#ffde00");
            case "AR" -> argentina();
            case "NL" -> h("#ae1c28", "#ffffff", "#21468b");
            case "SE" -> nordic("#006aa7", "#fecc00", null);
            case "FI" -> nordic("#ffffff", "#002f6c", null);
            case "NO" -> nordic("#ba0c2f", "#ffffff", "#00205b");
            case "CZ" -> czechia();
            case "RO" -> v("#002b7f", "#fcd116", "#ce1126");
            case "TW" -> taiwan();
            case "TH" -> h(new double[] {1, 1, 2, 1, 1}, "#a51931", "#ffffff", "#2d2a4a", "#ffffff", "#a51931");
            case "PH" -> philippines();
            case "MY" -> malaysia();
            case "HU" -> h("#ce2939", "#ffffff", "#477050");
            case "BE" -> v("#000000", "#ffd90c", "#ef3340");
            case "AT" -> h("#ed2939", "#ffffff", "#ed2939");
            case "PT" -> portugal();
            case "DK" -> nordic("#c60c30", "#ffffff", null);
            case "CH" -> switzerland();
            case "CL" -> chile();
            case "GR" -> greece();
            case "NZ" -> newZealand();
            case "IE" -> v("#169b62", "#ffffff", "#ff883e");
            case "SG" -> singapore();
            case "RS" -> h("#c6363c", "#0c4076", "#ffffff") + shield(7.2, 7.2, "#f1bf00", "#c6363c");
            case "HR" -> croatia();
            case "SK" -> h("#ffffff", "#0b4ea2", "#ee1c25") + shield(7.2, 7.2, "#ffffff", "#ee1c25");
            case "BG" -> h("#ffffff", "#00966e", "#d62612");
            case "KZ" -> kazakhstan();
            case "BY" -> belarus();
            case "ZA" -> southAfrica();
            case "SA" -> saudiArabia();
            case "AE" -> emirates();
            case "IL" -> israel();
            case "EG" -> h("#ce1126", "#ffffff", "#000000") + circle(11, 7, 1.1, "#c09300");
            case "PK" -> pakistan();
            case "BD" -> full("#006a4e") + circle(10.3, 7, 4.2, "#f42a41");
            case "LK" -> sriLanka();
            case "NP" -> nepal();
            case "MA" -> full("#c1272d") + starOutline(11, 7, 4.1, 1.6, "#006233", 0.9);
            case "DZ" -> algeria();
            case "TN" -> tunisia();
            case "PE" -> v("#d91023", "#ffffff", "#d91023");
            case "CO" -> h(new double[] {2, 1, 1}, "#fcd116", "#003893", "#ce1126");
            case "VE" -> venezuela();
            case "EC" -> h(new double[] {2, 1, 1}, "#ffdd00", "#034ea2", "#ed1c24") + circle(11, 7, 1.2, "#7a5c00");
            case "UY" -> uruguay();
            case "PY" -> h("#d52b1e", "#ffffff", "#0038a8") + circle(11, 7, 1.2, "#f1bf00");
            case "BO" -> h("#d52b1e", "#f9e300", "#007a33");
            case "CR" -> h(new double[] {1, 1, 2, 1, 1}, "#002b7f", "#ffffff", "#ce1126", "#ffffff", "#002b7f");
            case "PA" -> panama();
            case "GT" -> v("#4997d0", "#ffffff", "#4997d0") + circle(11, 7, 1.3, "#6c8f47");
            case "DO" -> dominicanRepublic();
            case "HN" -> honduras();
            case "SV" -> h("#0047ab", "#ffffff", "#0047ab") + circle(11, 7, 1.2, "#f1bf00");
            case "NI" -> h("#0067c6", "#ffffff", "#0067c6") + triangle("11,5.8 12.5,8.3 9.5,8.3", "#f1bf00");
            case "LU" -> h("#ef3340", "#ffffff", "#00a3e0");
            case "SI" -> h("#ffffff", "#005da4", "#ed1c24") + shield(7.2, 5.5, "#005da4", "#ed1c24");
            case "LT" -> h("#fdb913", "#006a44", "#c1272d");
            case "LV" -> h(new double[] {2, 1, 2}, "#9e3039", "#ffffff", "#9e3039");
            case "EE" -> h("#4891d9", "#000000", "#ffffff");
            case "IS" -> nordic("#02529c", "#ffffff", "#dc1e35");
            case "GE" -> georgia();
            case "AM" -> h("#d90012", "#0033a0", "#f2a800");
            case "AZ" -> azerbaijan();
            case "UZ" -> uzbekistan();
            case "KG" -> full("#e8112d") + sun(11, 7, 3.6, "#ffcd00");
            case "MN" -> mongolia();
            case "HK" -> hongKong();
            case "MO" -> macao();
            case "NG" -> v("#008751", "#ffffff", "#008751");
            case "KE" -> kenya();
            case "GH" -> h("#ce1126", "#fcd116", "#006b3f") + star(11, 7, 2.5, 1.0, "#000000");
            case "ET" -> h("#078930", "#fcd116", "#da121a") + circle(11, 7, 3.0, "#0f47af") + starOutline(11, 7, 2.0, 0.8, "#fcd116", 0.55);
            case "QA" -> serratedHoist("#ffffff", "#8a1538", 8);
            case "KW" -> kuwait();
            case "BH" -> serratedHoist("#ffffff", "#ce1126", 5);
            case "OM" -> oman();
            case "JO" -> jordan();
            case "LB" -> lebanon();
            case "CY" -> cyprus();
            case "MT" -> malta();
            default -> "";
        };
        return body.isBlank() ? "" : wrap(code, palette, x, y, body);
    }

    private static String china() {
        return full("#de2910")
                + star(5, 3.6, 2.2, 0.9, "#ffde00")
                + star(8.7, 2.1, 0.8, 0.35, "#ffde00")
                + star(10.2, 3.6, 0.8, 0.35, "#ffde00")
                + star(10.1, 5.7, 0.8, 0.35, "#ffde00")
                + star(8.5, 7.2, 0.8, 0.35, "#ffde00");
    }

    private static String unitedStates() {
        StringBuilder svg = new StringBuilder();
        double stripe = 14.0 / 13.0;
        for (int i = 0; i < 13; i++) {
            svg.append(rect(0, i * stripe, 22, stripe + 0.02, i % 2 == 0 ? "#b22234" : "#ffffff"));
        }
        svg.append(rect(0, 0, 9.4, stripe * 7, "#3c3b6e"));
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                svg.append(circle(1.1 + col * 1.7, 0.9 + row * 1.4, 0.22, "#ffffff"));
            }
        }
        return svg.toString();
    }

    private static String brazil() {
        return full("#009b3a")
                + polygon("11,1.4 20.2,7 11,12.6 1.8,7", "#ffdf00")
                + circle(11, 7, 3.6, "#002776")
                + line(7.7, 6.2, 14.3, 7.9, "#ffffff", 0.8);
    }

    private static String unionJack() {
        return unionJackAt(0, 0, 22, 14);
    }

    private static String unionJackAt(double x, double y, double width, double height) {
        return rect(x, y, width, height, "#012169")
                + line(x, y, x + width, y + height, "#ffffff", height * 0.26)
                + line(x + width, y, x, y + height, "#ffffff", height * 0.26)
                + line(x, y, x + width, y + height, "#c8102e", height * 0.12)
                + line(x + width, y, x, y + height, "#c8102e", height * 0.12)
                + rect(x + width * 0.40, y, width * 0.20, height, "#ffffff")
                + rect(x, y + height * 0.40, width, height * 0.20, "#ffffff")
                + rect(x + width * 0.45, y, width * 0.10, height, "#c8102e")
                + rect(x, y + height * 0.45, width, height * 0.10, "#c8102e");
    }

    private static String turkey() {
        return full("#e30a17")
                + circle(8.9, 7, 3.4, "#ffffff")
                + circle(10.0, 7, 2.7, "#e30a17")
                + star(14.1, 7, 2.0, 0.8, "#ffffff");
    }

    private static String canada() {
        return v(new double[] {1, 2, 1}, "#ff0000", "#ffffff", "#ff0000")
                + polygon("11,3.0 12.0,5.4 14.0,5.0 12.7,6.7 14.5,8.1 12.1,8.2 12.5,10.8 11,9.3 9.5,10.8 9.9,8.2 7.5,8.1 9.3,6.7 8.0,5.0 10.0,5.4", "#ff0000");
    }

    private static String southKorea() {
        return full("#ffffff")
                + path("M8.5 7a2.5 2.5 0 0 1 5 0a1.25 1.25 0 0 0-2.5 0a1.25 1.25 0 0 1-2.5 0", "#cd2e3a", null, 0)
                + path("M13.5 7a2.5 2.5 0 0 1-5 0a1.25 1.25 0 0 0 2.5 0a1.25 1.25 0 0 1 2.5 0", "#0047a0", null, 0)
                + line(4.0, 3.1, 6.5, 4.6, "#000000", 0.55)
                + line(15.5, 9.4, 18.0, 10.9, "#000000", 0.55)
                + line(15.6, 3.0, 18.1, 1.8, "#000000", 0.55)
                + line(4.0, 10.9, 6.5, 9.4, "#000000", 0.55);
    }

    private static String india() {
        String spokes = "";
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            spokes += line(11, 7, 11 + Math.cos(angle) * 2.1, 7 + Math.sin(angle) * 2.1, "#000080", 0.25);
        }
        return h("#ff9933", "#ffffff", "#138808") + circle(11, 7, 2.1, "none", "#000080", 0.55) + spokes;
    }

    private static String australia() {
        return full("#00008b")
                + unionJackAt(0, 0, 10.5, 7)
                + star(16, 10.5, 1.5, 0.6, "#ffffff")
                + star(18.5, 3.7, 1.0, 0.4, "#ffffff")
                + star(20.2, 6.7, 0.9, 0.35, "#ffffff")
                + star(17.8, 8.0, 0.9, 0.35, "#ffffff")
                + star(20.4, 11.1, 0.9, 0.35, "#ffffff");
    }

    private static String mexico() {
        return v("#006847", "#ffffff", "#ce1126")
                + circle(11, 7, 1.45, "#b38b00")
                + path("M9.7 7.6 C10.4 9 11.8 9 12.5 7.6", "none", "#2c7a35", 0.5);
    }

    private static String argentina() {
        return h("#74acdf", "#ffffff", "#74acdf")
                + sun(11, 7, 2.0, "#f6b40e");
    }

    private static String czechia() {
        return rect(0, 0, 22, 7, "#ffffff")
                + rect(0, 7, 22, 7, "#d7141a")
                + triangle("0,0 10,7 0,14", "#11457e");
    }

    private static String taiwan() {
        return full("#fe0000")
                + rect(0, 0, 11, 7, "#000095")
                + sun(5.5, 3.5, 2.2, "#ffffff");
    }

    private static String philippines() {
        return rect(0, 0, 22, 7, "#0038a8")
                + rect(0, 7, 22, 7, "#ce1126")
                + triangle("0,0 10.6,7 0,14", "#ffffff")
                + sun(3.8, 7, 1.4, "#fcd116")
                + star(1.7, 2.0, 0.7, 0.3, "#fcd116")
                + star(1.7, 12.0, 0.7, 0.3, "#fcd116")
                + star(8.0, 7.0, 0.7, 0.3, "#fcd116");
    }

    private static String malaysia() {
        StringBuilder svg = new StringBuilder();
        double stripe = 14.0 / 14.0;
        for (int i = 0; i < 14; i++) {
            svg.append(rect(0, i * stripe, 22, stripe + 0.02, i % 2 == 0 ? "#cc0001" : "#ffffff"));
        }
        svg.append(rect(0, 0, 10.7, 8, "#010066"));
        svg.append(circle(4.5, 4, 2.5, "#ffcc00"));
        svg.append(circle(5.3, 4, 2.0, "#010066"));
        svg.append(star(8.0, 4, 1.5, 0.6, "#ffcc00"));
        return svg.toString();
    }

    private static String portugal() {
        return v(new double[] {2, 3}, "#006600", "#ff0000")
                + circle(8.9, 7, 2.1, "#ffcc00")
                + rect(7.9, 5.7, 2, 2.6, "#ffffff");
    }

    private static String switzerland() {
        return full("#d52b1e")
                + rect(9.0, 3.2, 4.0, 7.6, "#ffffff")
                + rect(6.4, 5.8, 9.2, 2.4, "#ffffff");
    }

    private static String chile() {
        return rect(0, 0, 22, 7, "#ffffff")
                + rect(0, 7, 22, 7, "#d52b1e")
                + rect(0, 0, 8, 7, "#0039a6")
                + star(4, 3.5, 1.6, 0.65, "#ffffff");
    }

    private static String greece() {
        StringBuilder svg = new StringBuilder();
        double stripe = 14.0 / 9.0;
        for (int i = 0; i < 9; i++) {
            svg.append(rect(0, i * stripe, 22, stripe + 0.02, i % 2 == 0 ? "#0d5eaf" : "#ffffff"));
        }
        svg.append(rect(0, 0, 7.8, 7.8, "#0d5eaf"));
        svg.append(rect(3.0, 0, 1.8, 7.8, "#ffffff"));
        svg.append(rect(0, 3.0, 7.8, 1.8, "#ffffff"));
        return svg.toString();
    }

    private static String newZealand() {
        return full("#00247d")
                + unionJackAt(0, 0, 10.5, 7)
                + starOutline(16.2, 4.0, 1.25, 0.5, "#ffffff", 0.8)
                + star(16.2, 4.0, 0.9, 0.35, "#cc142b")
                + starOutline(19.3, 6.0, 1.25, 0.5, "#ffffff", 0.8)
                + star(19.3, 6.0, 0.9, 0.35, "#cc142b")
                + starOutline(15.2, 9.3, 1.25, 0.5, "#ffffff", 0.8)
                + star(15.2, 9.3, 0.9, 0.35, "#cc142b")
                + starOutline(19.0, 11.2, 1.25, 0.5, "#ffffff", 0.8)
                + star(19.0, 11.2, 0.9, 0.35, "#cc142b");
    }

    private static String singapore() {
        return h("#ef3340", "#ffffff")
                + circle(5.0, 3.7, 2.3, "#ffffff")
                + circle(5.8, 3.7, 1.9, "#ef3340")
                + star(8.4, 2.2, 0.55, 0.22, "#ffffff")
                + star(9.5, 3.4, 0.55, 0.22, "#ffffff")
                + star(8.9, 5.0, 0.55, 0.22, "#ffffff")
                + star(7.4, 5.0, 0.55, 0.22, "#ffffff")
                + star(6.8, 3.4, 0.55, 0.22, "#ffffff");
    }

    private static String croatia() {
        return h("#ff0000", "#ffffff", "#171796")
                + rect(9.2, 5.1, 3.6, 3.6, "#ff0000")
                + rect(9.2, 5.1, 1.2, 1.2, "#ffffff")
                + rect(11.6, 5.1, 1.2, 1.2, "#ffffff")
                + rect(10.4, 6.3, 1.2, 1.2, "#ffffff")
                + rect(9.2, 7.5, 1.2, 1.2, "#ffffff")
                + rect(11.6, 7.5, 1.2, 1.2, "#ffffff");
    }

    private static String kazakhstan() {
        return full("#00afca")
                + rect(2.0, 1.2, 0.7, 11.6, "#ffce00")
                + sun(11.5, 6.2, 2.7, "#ffce00")
                + path("M8.4 9.6 C10.5 11.0 12.8 11.0 14.9 9.6", "none", "#ffce00", 0.9);
    }

    private static String belarus() {
        return rect(0, 0, 22, 9.5, "#d22730")
                + rect(0, 9.5, 22, 4.5, "#00af66")
                + rect(0, 0, 3.2, 14, "#ffffff")
                + rect(0.6, 1.1, 0.7, 1.0, "#d22730")
                + rect(1.9, 3.1, 0.7, 1.0, "#d22730")
                + rect(0.6, 5.1, 0.7, 1.0, "#d22730")
                + rect(1.9, 7.1, 0.7, 1.0, "#d22730")
                + rect(0.6, 9.1, 0.7, 1.0, "#d22730")
                + rect(1.9, 11.1, 0.7, 1.0, "#d22730");
    }

    private static String southAfrica() {
        return rect(0, 0, 22, 7, "#de3831")
                + rect(0, 7, 22, 7, "#002395")
                + polygon("0,0 11,7 0,14", "#ffffff")
                + polygon("0,1.4 8.8,7 0,12.6", "#ffb612")
                + polygon("0,2.7 6.8,7 0,11.3", "#000000")
                + polygon("0,0 12.5,7 0,14 0,10.8 7.2,7 0,3.2", "#007a4d");
    }

    private static String saudiArabia() {
        return full("#006c35")
                + path("M6 5.7 H16.2", "none", "#ffffff", 0.7)
                + path("M6.4 8.9 H15.8", "none", "#ffffff", 0.55)
                + path("M7.1 10.2 H14.8", "none", "#ffffff", 0.45);
    }

    private static String emirates() {
        return rect(0, 0, 5.5, 14, "#ff0000")
                + rect(5.5, 0, 16.5, 14.0 / 3.0, "#009739")
                + rect(5.5, 14.0 / 3.0, 16.5, 14.0 / 3.0, "#ffffff")
                + rect(5.5, 28.0 / 3.0, 16.5, 14.0 / 3.0, "#000000");
    }

    private static String israel() {
        return full("#ffffff")
                + rect(0, 1.6, 22, 1.6, "#0038b8")
                + rect(0, 10.8, 22, 1.6, "#0038b8")
                + starOfDavid(11, 7, 3.0, "#0038b8");
    }

    private static String pakistan() {
        return rect(0, 0, 5.0, 14, "#ffffff")
                + rect(5.0, 0, 17.0, 14, "#01411c")
                + circle(12.5, 7, 3.4, "#ffffff")
                + circle(13.5, 7, 3.0, "#01411c")
                + star(16.2, 4.8, 1.4, 0.55, "#ffffff");
    }

    private static String sriLanka() {
        return full("#ffbe29")
                + rect(1.2, 1.2, 3.0, 11.6, "#00534e")
                + rect(4.2, 1.2, 3.0, 11.6, "#eb7400")
                + rect(8.0, 1.2, 12.8, 11.6, "#8d153a")
                + circle(14.4, 7, 2.0, "#ffbe29");
    }

    private static String nepal() {
        return polygon("1,0 14.5,6.2 5.1,6.2 17,14 1,14", "#003893")
                + polygon("2.4,1.9 11.2,5.7 3.9,5.7 13.3,12.7 2.4,12.7", "#dc143c")
                + circle(5.1, 5.0, 1.0, "#ffffff")
                + sun(6.4, 10.2, 1.2, "#ffffff");
    }

    private static String algeria() {
        return rect(0, 0, 11, 14, "#006233")
                + rect(11, 0, 11, 14, "#ffffff")
                + circle(11.7, 7, 3.0, "#d21034")
                + circle(12.6, 7, 2.4, "#ffffff")
                + star(14.4, 7, 1.6, 0.65, "#d21034");
    }

    private static String tunisia() {
        return full("#e70013")
                + circle(11, 7, 4.3, "#ffffff")
                + circle(10.6, 7, 2.2, "#e70013")
                + circle(11.3, 7, 1.8, "#ffffff")
                + star(13.3, 7, 1.4, 0.55, "#e70013");
    }

    private static String venezuela() {
        return h("#ffcc00", "#00247d", "#cf142b")
                + star(7.7, 7, 0.65, 0.25, "#ffffff")
                + star(9.6, 6.4, 0.65, 0.25, "#ffffff")
                + star(11.5, 6.2, 0.65, 0.25, "#ffffff")
                + star(13.4, 6.4, 0.65, 0.25, "#ffffff")
                + star(15.3, 7, 0.65, 0.25, "#ffffff");
    }

    private static String uruguay() {
        StringBuilder svg = new StringBuilder();
        double stripe = 14.0 / 9.0;
        for (int i = 0; i < 9; i++) {
            svg.append(rect(0, i * stripe, 22, stripe + 0.02, i % 2 == 0 ? "#ffffff" : "#0038a8"));
        }
        svg.append(rect(0, 0, 8, stripe * 5, "#ffffff"));
        svg.append(sun(4, 3.8, 1.8, "#fcd116"));
        return svg.toString();
    }

    private static String panama() {
        return rect(0, 0, 11, 7, "#ffffff")
                + rect(11, 0, 11, 7, "#d21034")
                + rect(0, 7, 11, 7, "#005293")
                + rect(11, 7, 11, 7, "#ffffff")
                + star(5.5, 3.5, 1.4, 0.55, "#005293")
                + star(16.5, 10.5, 1.4, 0.55, "#d21034");
    }

    private static String dominicanRepublic() {
        return rect(0, 0, 9.3, 5.6, "#002d62")
                + rect(12.7, 0, 9.3, 5.6, "#ce1126")
                + rect(0, 8.4, 9.3, 5.6, "#ce1126")
                + rect(12.7, 8.4, 9.3, 5.6, "#002d62")
                + rect(9.3, 0, 3.4, 14, "#ffffff")
                + rect(0, 5.6, 22, 2.8, "#ffffff")
                + circle(11, 7, 1.0, "#006b3f");
    }

    private static String honduras() {
        return h("#00bce4", "#ffffff", "#00bce4")
                + star(8.5, 6.2, 0.5, 0.2, "#00bce4")
                + star(11, 6.2, 0.5, 0.2, "#00bce4")
                + star(13.5, 6.2, 0.5, 0.2, "#00bce4")
                + star(9.8, 8.0, 0.5, 0.2, "#00bce4")
                + star(12.2, 8.0, 0.5, 0.2, "#00bce4");
    }

    private static String georgia() {
        return full("#ffffff")
                + rect(9.2, 0, 3.6, 14, "#ff0000")
                + rect(0, 5.2, 22, 3.6, "#ff0000")
                + cross(4.6, 2.7, 1.9, "#ff0000")
                + cross(17.4, 2.7, 1.9, "#ff0000")
                + cross(4.6, 11.3, 1.9, "#ff0000")
                + cross(17.4, 11.3, 1.9, "#ff0000");
    }

    private static String azerbaijan() {
        return h("#00b5e2", "#ef3340", "#509e2f")
                + circle(10.2, 7, 2.0, "#ffffff")
                + circle(10.9, 7, 1.6, "#ef3340")
                + star(13.2, 7, 1.1, 0.45, "#ffffff");
    }

    private static String uzbekistan() {
        return rect(0, 0, 22, 4.7, "#1eb5e9")
                + rect(0, 4.7, 22, 0.5, "#ce1126")
                + rect(0, 5.2, 22, 3.6, "#ffffff")
                + rect(0, 8.8, 22, 0.5, "#ce1126")
                + rect(0, 9.3, 22, 4.7, "#009739")
                + circle(4.2, 2.4, 1.6, "#ffffff")
                + circle(4.8, 2.4, 1.3, "#1eb5e9")
                + star(7.0, 1.6, 0.45, 0.18, "#ffffff")
                + star(8.6, 1.6, 0.45, 0.18, "#ffffff")
                + star(10.2, 1.6, 0.45, 0.18, "#ffffff");
    }

    private static String mongolia() {
        return v("#da2032", "#0066b3", "#da2032")
                + circle(3.7, 4.8, 0.8, "#ffd900")
                + circle(3.7, 8.5, 0.8, "#ffd900")
                + rect(3.2, 5.8, 1.0, 1.8, "#ffd900");
    }

    private static String hongKong() {
        String petals = "";
        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(i * 72 - 90);
            double cx = 11 + Math.cos(angle) * 2.1;
            double cy = 7 + Math.sin(angle) * 2.1;
            petals += circle(cx, cy, 1.35, "#ffffff");
        }
        return full("#de2910") + petals + circle(11, 7, 1.2, "#de2910");
    }

    private static String macao() {
        return full("#00785e")
                + star(11, 2.2, 0.6, 0.25, "#ffde00")
                + star(8.9, 3.0, 0.45, 0.18, "#ffde00")
                + star(13.1, 3.0, 0.45, 0.18, "#ffde00")
                + path("M7.3 8.4 C9.0 6.1 13.0 6.1 14.7 8.4 C12.8 9.1 9.2 9.1 7.3 8.4", "#ffffff", null, 0)
                + rect(6.4, 10.0, 9.2, 0.7, "#ffffff")
                + rect(7.3, 11.3, 7.4, 0.7, "#ffffff");
    }

    private static String kenya() {
        return rect(0, 0, 22, 4.2, "#000000")
                + rect(0, 4.2, 22, 0.7, "#ffffff")
                + rect(0, 4.9, 22, 4.2, "#bb0000")
                + rect(0, 9.1, 22, 0.7, "#ffffff")
                + rect(0, 9.8, 22, 4.2, "#006600")
                + path("M9.4 3.4 C12.3 4.7 12.3 9.3 9.4 10.6 C7.5 8.6 7.5 5.4 9.4 3.4", "#bb0000", "#ffffff", 0.5)
                + path("M12.6 3.4 C9.7 4.7 9.7 9.3 12.6 10.6 C14.5 8.6 14.5 5.4 12.6 3.4", "#000000", "#ffffff", 0.5);
    }

    private static String serratedHoist(String hoist, String field, int teeth) {
        StringBuilder points = new StringBuilder("0,0 7,0 ");
        double step = 14.0 / teeth;
        for (int i = 0; i < teeth; i++) {
            points.append(format("3.8,%.3f 7,%.3f ", i * step + step / 2.0, (i + 1) * step));
        }
        points.append("0,14");
        return full(field) + polygon(points.toString(), hoist);
    }

    private static String kuwait() {
        return rect(0, 0, 22, 14.0 / 3.0, "#007a3d")
                + rect(0, 14.0 / 3.0, 22, 14.0 / 3.0, "#ffffff")
                + rect(0, 28.0 / 3.0, 22, 14.0 / 3.0, "#ce1126")
                + polygon("0,0 6.0,4.7 6.0,9.3 0,14", "#000000");
    }

    private static String oman() {
        return rect(0, 0, 22, 14.0 / 3.0, "#ffffff")
                + rect(0, 14.0 / 3.0, 22, 14.0 / 3.0, "#db161b")
                + rect(0, 28.0 / 3.0, 22, 14.0 / 3.0, "#008000")
                + rect(0, 0, 5.5, 14, "#db161b")
                + cross(2.75, 3.0, 1.5, "#ffffff");
    }

    private static String jordan() {
        return h("#000000", "#ffffff", "#007a3d")
                + triangle("0,0 10.5,7 0,14", "#ce1126")
                + star(3.6, 7, 1.0, 0.4, "#ffffff");
    }

    private static String lebanon() {
        return h(new double[] {1, 2, 1}, "#ed1c24", "#ffffff", "#ed1c24")
                + triangle("11,4.4 8.2,9.2 13.8,9.2", "#007a3d")
                + rect(10.3, 8.6, 1.4, 2.0, "#007a3d");
    }

    private static String cyprus() {
        return full("#ffffff")
                + path("M8.5 6.2 C10 4.8 13.5 5 14.5 6.6 C13.1 8 10.1 8.3 8.5 6.2", "#d57800", null, 0)
                + path("M8 10 C9.4 11 10.5 10.4 11 9.6 M14 10 C12.6 11 11.5 10.4 11 9.6", "none", "#4e8a2d", 0.45);
    }

    private static String malta() {
        return rect(0, 0, 11, 14, "#ffffff")
                + rect(11, 0, 11, 14, "#cf142b")
                + rect(2.0, 2.0, 2.0, 0.7, "#8a8f98")
                + rect(2.65, 1.35, 0.7, 2.0, "#8a8f98");
    }

    private static String wrap(String code, SvgTheme.Palette palette, int x, int y, String body) {
        return """
                <g role="img" aria-label="%s">
                  <title>%s</title>
                  <rect x="%d" y="%d" width="%d" height="%d" rx="3" fill="%s"/>
                  <svg x="%d" y="%d" width="22" height="14" viewBox="0 0 22 14" preserveAspectRatio="none" overflow="hidden">
                    %s
                  </svg>
                </g>
                """.formatted(code, code, x, y, WIDTH, HEIGHT, palette.border(), x + 2, y + 2, body);
    }

    private static String normalize(String countryCode) {
        return countryCode == null ? "" : countryCode.trim().toUpperCase(Locale.ROOT);
    }

    private static String full(String color) {
        return rect(0, 0, 22, 14, color);
    }

    private static String h(String... colors) {
        double[] weights = new double[colors.length];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = 1;
        }
        return h(weights, colors);
    }

    private static String h(double[] weights, String... colors) {
        StringBuilder svg = new StringBuilder();
        double total = total(weights);
        double currentY = 0;
        for (int i = 0; i < colors.length; i++) {
            double height = 14 * weights[i] / total;
            svg.append(rect(0, currentY, 22, height + 0.02, colors[i]));
            currentY += height;
        }
        return svg.toString();
    }

    private static String v(String... colors) {
        double[] weights = new double[colors.length];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = 1;
        }
        return v(weights, colors);
    }

    private static String v(double[] weights, String... colors) {
        StringBuilder svg = new StringBuilder();
        double total = total(weights);
        double currentX = 0;
        for (int i = 0; i < colors.length; i++) {
            double width = 22 * weights[i] / total;
            svg.append(rect(currentX, 0, width + 0.02, 14, colors[i]));
            currentX += width;
        }
        return svg.toString();
    }

    private static String nordic(String background, String cross, String innerCross) {
        String svg = full(background);
        if (innerCross == null) {
            return svg + rect(6.5, 0, 3.0, 14, cross) + rect(0, 5.5, 22, 3.0, cross);
        }
        return svg
                + rect(5.6, 0, 4.8, 14, cross)
                + rect(0, 4.6, 22, 4.8, cross)
                + rect(6.9, 0, 2.2, 14, innerCross)
                + rect(0, 5.9, 22, 2.2, innerCross);
    }

    private static String rect(double x, double y, double width, double height, String color) {
        return format("<rect x=\"%.3f\" y=\"%.3f\" width=\"%.3f\" height=\"%.3f\" fill=\"%s\"/>%n",
                x, y, width, height, color);
    }

    private static String circle(double cx, double cy, double r, String color) {
        return format("<circle cx=\"%.3f\" cy=\"%.3f\" r=\"%.3f\" fill=\"%s\"/>%n", cx, cy, r, color);
    }

    private static String circle(double cx, double cy, double r, String fill, String stroke, double strokeWidth) {
        return format("<circle cx=\"%.3f\" cy=\"%.3f\" r=\"%.3f\" fill=\"%s\" stroke=\"%s\" stroke-width=\"%.3f\"/>%n",
                cx, cy, r, fill, stroke, strokeWidth);
    }

    private static String line(double x1, double y1, double x2, double y2, String color, double strokeWidth) {
        return format("<line x1=\"%.3f\" y1=\"%.3f\" x2=\"%.3f\" y2=\"%.3f\" stroke=\"%s\" stroke-width=\"%.3f\" stroke-linecap=\"butt\"/>%n",
                x1, y1, x2, y2, color, strokeWidth);
    }

    private static String polygon(String points, String color) {
        return "<polygon points=\"%s\" fill=\"%s\"/>%n".formatted(points, color);
    }

    private static String triangle(String points, String color) {
        return polygon(points, color);
    }

    private static String path(String d, String fill, String stroke, double strokeWidth) {
        String strokeAttrs = stroke == null ? "" : format(" stroke=\"%s\" stroke-width=\"%.3f\"", stroke, strokeWidth);
        return "<path d=\"%s\" fill=\"%s\"%s/>%n".formatted(d, fill, strokeAttrs);
    }

    private static String star(double cx, double cy, double outerRadius, double innerRadius, String color) {
        return polygon(starPoints(cx, cy, outerRadius, innerRadius), color);
    }

    private static String starOutline(double cx, double cy, double outerRadius, double innerRadius, String color, double strokeWidth) {
        return format("<polygon points=\"%s\" fill=\"none\" stroke=\"%s\" stroke-width=\"%.3f\" stroke-linejoin=\"round\"/>%n",
                starPoints(cx, cy, outerRadius, innerRadius), color, strokeWidth);
    }

    private static String starOfDavid(double cx, double cy, double radius, String color) {
        return format("""
                <polygon points="%.3f,%.3f %.3f,%.3f %.3f,%.3f" fill="none" stroke="%s" stroke-width="0.650"/>
                <polygon points="%.3f,%.3f %.3f,%.3f %.3f,%.3f" fill="none" stroke="%s" stroke-width="0.650"/>
                """,
                cx, cy - radius, cx - radius * 0.86, cy + radius / 2, cx + radius * 0.86, cy + radius / 2, color,
                cx, cy + radius, cx - radius * 0.86, cy - radius / 2, cx + radius * 0.86, cy - radius / 2, color);
    }

    private static String sun(double cx, double cy, double radius, String color) {
        StringBuilder svg = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30);
            svg.append(line(cx, cy, cx + Math.cos(angle) * radius * 1.45, cy + Math.sin(angle) * radius * 1.45,
                    color, Math.max(0.35, radius / 5)));
        }
        svg.append(circle(cx, cy, radius, color));
        return svg.toString();
    }

    private static String shield(double cx, double cy, String border, String fill) {
        return polygon(format("%.3f,%.3f %.3f,%.3f %.3f,%.3f %.3f,%.3f",
                cx - 1.7, cy - 2.0, cx + 1.7, cy - 2.0, cx + 1.2, cy + 1.5, cx, cy + 2.7), border)
                + polygon(format("%.3f,%.3f %.3f,%.3f %.3f,%.3f %.3f,%.3f",
                cx - 1.1, cy - 1.3, cx + 1.1, cy - 1.3, cx + 0.8, cy + 1.0, cx, cy + 1.8), fill);
    }

    private static String cross(double cx, double cy, double size, String color) {
        double arm = size / 3.0;
        return rect(cx - arm / 2, cy - size / 2, arm, size, color)
                + rect(cx - size / 2, cy - arm / 2, size, arm, color);
    }

    private static String starPoints(double cx, double cy, double outerRadius, double innerRadius) {
        StringBuilder points = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            double angle = Math.toRadians(-90 + i * 36);
            double radius = i % 2 == 0 ? outerRadius : innerRadius;
            if (i > 0) {
                points.append(' ');
            }
            points.append(format("%.3f,%.3f", cx + Math.cos(angle) * radius, cy + Math.sin(angle) * radius));
        }
        return points.toString();
    }

    private static double total(double[] values) {
        double total = 0;
        for (double value : values) {
            total += value;
        }
        return total;
    }

    private static String format(String template, Object... values) {
        return String.format(Locale.ROOT, template, values);
    }
}
