package io.joshuasalcedo.valueobject.geo;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.ValueObject;


import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.joshuasalcedo.valueobject.geo.Continent.*;

/**
 * Country value object representing a sovereign state or territory.
 *
 * <p>Each country has an ISO 3166-1 alpha-2 code, a display name, a primary {@link Demonym},
 * and a {@link Continent}. A country may also have additional demonyms beyond the primary one
 * (e.g. Philippines has Filipino, Pinoy/Pinay).</p>
 *
 * @author JoshuaSalcedo
 * @since 2/25/2026
 * @see Demonym
 * @see Continent
 */
public enum Country implements ValueObject {

    /** Afghanistan ({@code AF}). */
    AFGHANISTAN("AF", "Afghanistan", Demonym.AFGHAN, Continent.ASIA),
    /** Åland Islands ({@code AX}). */
    ALAND_ISLANDS("AX", "Åland Islands", Demonym.ALANDER, Continent.EUROPE),
    /** Albania ({@code AL}). */
    ALBANIA("AL", "Albania", Demonym.ALBANIAN, Continent.EUROPE),
    /** Algeria ({@code DZ}). */
    ALGERIA("DZ", "Algeria", Demonym.ALGERIAN, Continent.AFRICA),
    /** American Samoa ({@code AS}). */
    AMERICAN_SAMOA("AS", "American Samoa", Demonym.AMERICAN_SAMOAN, Continent.OCEANIA),
    /** Andorra ({@code AD}). */
    ANDORRA("AD", "Andorra", Demonym.ANDORRAN, Continent.EUROPE),
    /** Angola ({@code AO}). */
    ANGOLA("AO", "Angola", Demonym.ANGOLAN, Continent.AFRICA),
    /** Anguilla ({@code AI}). */
    ANGUILLA("AI", "Anguilla", Demonym.ANGUILLIAN, Continent.NORTH_AMERICA),
    /** Antarctica ({@code AQ}). */
    ANTARCTICA("AQ", "Antarctica", Demonym.UNKNOWN, Continent.ANTARCTICA),
    /** Antigua &amp; Barbuda ({@code AG}). */
    ANTIGUA_AND_BARBUDA("AG", "Antigua & Barbuda", Demonym.ANTIGUAN, Continent.NORTH_AMERICA),
    /** Argentina ({@code AR}). */
    ARGENTINA("AR", "Argentina", Demonym.ARGENTINE, Continent.SOUTH_AMERICA),
    /** Armenia ({@code AM}). */
    ARMENIA("AM", "Armenia", Demonym.ARMENIAN, Continent.ASIA),
    /** Aruba ({@code AW}). */
    ARUBA("AW", "Aruba", Demonym.ARUBAN, Continent.NORTH_AMERICA),
    /** Australia ({@code AU}). */
    AUSTRALIA("AU", "Australia", Demonym.AUSTRALIAN, Continent.OCEANIA),
    /** Austria ({@code AT}). */
    AUSTRIA("AT", "Austria", Demonym.AUSTRIAN, Continent.EUROPE),
    /** Azerbaijan ({@code AZ}). */
    AZERBAIJAN("AZ", "Azerbaijan", Demonym.AZERBAIJANI, Continent.ASIA),
    /** Bahamas ({@code BS}). */
    BAHAMAS("BS", "Bahamas", Demonym.BAHAMIAN, Continent.NORTH_AMERICA),
    /** Bahrain ({@code BH}). */
    BAHRAIN("BH", "Bahrain", Demonym.BAHRAINI, Continent.ASIA),
    /** Bangladesh ({@code BD}). */
    BANGLADESH("BD", "Bangladesh", Demonym.BANGLADESHI, Continent.ASIA),
    /** Barbados ({@code BB}). */
    BARBADOS("BB", "Barbados", Demonym.BARBADIAN, Continent.NORTH_AMERICA),
    /** Belarus ({@code BY}). */
    BELARUS("BY", "Belarus", Demonym.BELARUSIAN, Continent.EUROPE),
    /** Belgium ({@code BE}). */
    BELGIUM("BE", "Belgium", Demonym.BELGIAN, Continent.EUROPE),
    /** Belize ({@code BZ}). */
    BELIZE("BZ", "Belize", Demonym.BELIZEAN, Continent.NORTH_AMERICA),
    /** Benin ({@code BJ}). */
    BENIN("BJ", "Benin", Demonym.BENINESE, Continent.AFRICA),
    /** Bermuda ({@code BM}). */
    BERMUDA("BM", "Bermuda", Demonym.BERMUDIAN, Continent.NORTH_AMERICA),
    /** Bhutan ({@code BT}). */
    BHUTAN("BT", "Bhutan", Demonym.BHUTANESE, Continent.ASIA),
    /** Bolivia ({@code BO}). */
    BOLIVIA("BO", "Bolivia", Demonym.BOLIVIAN, Continent.SOUTH_AMERICA),
    /** Bosnia &amp; Herzegovina ({@code BA}). */
    BOSNIA_AND_HERZEGOVINA("BA", "Bosnia & Herzegovina", Demonym.BOSNIAN, Continent.EUROPE),
    /** Botswana ({@code BW}). */
    BOTSWANA("BW", "Botswana", Demonym.BOTSWANAN, Continent.AFRICA),
    /** Bouvet Island ({@code BV}). */
    BOUVET_ISLAND("BV", "Bouvet Island", Demonym.UNKNOWN, Continent.ANTARCTICA),
    /** Brazil ({@code BR}). */
    BRAZIL("BR", "Brazil", Demonym.BRAZILIAN, Continent.SOUTH_AMERICA),
    /** British Indian Ocean Territory ({@code IO}). */
    BRITISH_INDIAN_OCEAN_TERRITORY("IO", "British Indian Ocean Territory", Demonym.UNKNOWN, Continent.ASIA),
    /** British Virgin Islands ({@code VG}). */
    BRITISH_VIRGIN_ISLANDS("VG", "British Virgin Islands", Demonym.VIRGIN_ISLANDER, Continent.NORTH_AMERICA),
    /** Brunei ({@code BN}). */
    BRUNEI("BN", "Brunei", Demonym.BRUNEIAN, Continent.ASIA),
    /** Bulgaria ({@code BG}). */
    BULGARIA("BG", "Bulgaria", Demonym.BULGARIAN, Continent.EUROPE),
    /** Burkina Faso ({@code BF}). */
    BURKINA_FASO("BF", "Burkina Faso", Demonym.BURKINABE, Continent.AFRICA),
    /** Burundi ({@code BI}). */
    BURUNDI("BI", "Burundi", Demonym.BURUNDIAN, Continent.AFRICA),
    /** Cambodia ({@code KH}). */
    CAMBODIA("KH", "Cambodia", Demonym.CAMBODIAN, Continent.ASIA),
    /** Cameroon ({@code CM}). */
    CAMEROON("CM", "Cameroon", Demonym.CAMEROONIAN, Continent.AFRICA),
    /** Canada ({@code CA}). */
    CANADA("CA", "Canada", Demonym.CANADIAN, Continent.NORTH_AMERICA),
    /** Cape Verde ({@code CV}). */
    CAPE_VERDE("CV", "Cape Verde", Demonym.CABO_VERDEAN, Continent.AFRICA),
    /** Caribbean Netherlands ({@code BQ}). */
    CARIBBEAN_NETHERLANDS("BQ", "Caribbean Netherlands", Demonym.UNKNOWN, Continent.NORTH_AMERICA),
    /** Cayman Islands ({@code KY}). */
    CAYMAN_ISLANDS("KY", "Cayman Islands", Demonym.CAYMANIAN, Continent.NORTH_AMERICA),
    /** Central African Republic ({@code CF}). */
    CENTRAL_AFRICAN_REPUBLIC("CF", "Central African Republic", Demonym.CENTRAL_AFRICAN, Continent.AFRICA),
    /** Chad ({@code TD}). */
    CHAD("TD", "Chad", Demonym.CHADIAN, Continent.AFRICA),
    /** Chile ({@code CL}). */
    CHILE("CL", "Chile", Demonym.CHILEAN, Continent.SOUTH_AMERICA),
    /** China ({@code CN}). */
    CHINA("CN", "China", Demonym.CHINESE, Continent.ASIA),
    /** Christmas Island ({@code CX}). */
    CHRISTMAS_ISLAND("CX", "Christmas Island", Demonym.UNKNOWN, Continent.ASIA),
    /** Cocos (Keeling) Islands ({@code CC}). */
    COCOS_KEELING_ISLANDS("CC", "Cocos (Keeling) Islands", Demonym.UNKNOWN, Continent.ASIA),
    /** Colombia ({@code CO}). */
    COLOMBIA("CO", "Colombia", Demonym.COLOMBIAN, Continent.SOUTH_AMERICA),
    /** Comoros ({@code KM}). */
    COMOROS("KM", "Comoros", Demonym.COMORIAN, Continent.AFRICA),
    /** Congo ({@code CG}). */
    CONGO("CG", "Congo", Demonym.CONGOLESE, Continent.AFRICA),
    /** Cook Islands ({@code CK}). */
    COOK_ISLANDS("CK", "Cook Islands", Demonym.COOK_ISLANDER, Continent.OCEANIA),
    /** Costa Rica ({@code CR}). */
    COSTA_RICA("CR", "Costa Rica", Demonym.COSTA_RICAN, Continent.NORTH_AMERICA),
    /** Côte d'Ivoire ({@code CI}). */
    COTE_D_IVOIRE("CI", "Côte d'Ivoire", Demonym.IVORIAN, Continent.AFRICA),
    /** Croatia ({@code HR}). */
    CROATIA("HR", "Croatia", Demonym.CROATIAN, Continent.EUROPE),
    /** Cuba ({@code CU}). */
    CUBA("CU", "Cuba", Demonym.CUBAN, Continent.NORTH_AMERICA),
    /** Curaçao ({@code CW}). */
    CURACAO("CW", "Curaçao", Demonym.CURACAOAN, Continent.NORTH_AMERICA),
    /** Cyprus ({@code CY}). */
    CYPRUS("CY", "Cyprus", Demonym.CYPRIOT, Continent.EUROPE),
    /** Czechia ({@code CZ}). */
    CZECHIA("CZ", "Czechia", Demonym.CZECH, Continent.EUROPE),
    /** Denmark ({@code DK}). */
    DENMARK("DK", "Denmark", Demonym.DANISH, Continent.EUROPE),
    /** Djibouti ({@code DJ}). */
    DJIBOUTI("DJ", "Djibouti", Demonym.DJIBOUTIAN, Continent.AFRICA),
    /** Dominica ({@code DM}). */
    DOMINICA("DM", "Dominica", Demonym.DOMINICAN, Continent.NORTH_AMERICA),
    /** Dominican Republic ({@code DO}). */
    DOMINICAN_REPUBLIC("DO", "Dominican Republic", Demonym.DOMINICAN, Continent.NORTH_AMERICA),
    /** DR Congo ({@code CD}). */
    DR_CONGO("CD", "DR Congo", Demonym.CONGOLESE, Continent.AFRICA),
    /** Ecuador ({@code EC}). */
    ECUADOR("EC", "Ecuador", Demonym.ECUADORIAN, Continent.SOUTH_AMERICA),
    /** Egypt ({@code EG}). */
    EGYPT("EG", "Egypt", Demonym.EGYPTIAN, Continent.AFRICA),
    /** El Salvador ({@code SV}). */
    EL_SALVADOR("SV", "El Salvador", Demonym.SALVADORAN, Continent.NORTH_AMERICA),
    /** Equatorial Guinea ({@code GQ}). */
    EQUATORIAL_GUINEA("GQ", "Equatorial Guinea", Demonym.EQUATORIAL_GUINEAN, Continent.AFRICA),
    /** Eritrea ({@code ER}). */
    ERITREA("ER", "Eritrea", Demonym.ERITREAN, Continent.AFRICA),
    /** Estonia ({@code EE}). */
    ESTONIA("EE", "Estonia", Demonym.ESTONIAN, Continent.EUROPE),
    /** Eswatini ({@code SZ}). */
    ESWATINI("SZ", "Eswatini", Demonym.SWAZI, Continent.AFRICA),
    /** Ethiopia ({@code ET}). */
    ETHIOPIA("ET", "Ethiopia", Demonym.ETHIOPIAN, Continent.AFRICA),
    /** Falkland Islands ({@code FK}). */
    FALKLAND_ISLANDS("FK", "Falkland Islands", Demonym.FALKLAND_ISLANDER, Continent.SOUTH_AMERICA),
    /** Faroe Islands ({@code FO}). */
    FAROE_ISLANDS("FO", "Faroe Islands", Demonym.FAROESE, Continent.EUROPE),
    /** Fiji ({@code FJ}). */
    FIJI("FJ", "Fiji", Demonym.FIJIAN, Continent.OCEANIA),
    /** Finland ({@code FI}). */
    FINLAND("FI", "Finland", Demonym.FINNISH, Continent.EUROPE),
    /** France ({@code FR}). */
    FRANCE("FR", "France", Demonym.FRENCH, Continent.EUROPE),
    /** French Guiana ({@code GF}). */
    FRENCH_GUIANA("GF", "French Guiana", Demonym.FRENCH_GUIANESE, Continent.SOUTH_AMERICA),
    /** French Polynesia ({@code PF}). */
    FRENCH_POLYNESIA("PF", "French Polynesia", Demonym.FRENCH_POLYNESIAN, Continent.OCEANIA),
    /** French Southern Territories ({@code TF}). */
    FRENCH_SOUTHERN_TERRITORIES("TF", "French Southern Territories", Demonym.UNKNOWN, Continent.ANTARCTICA),
    /** Gabon ({@code GA}). */
    GABON("GA", "Gabon", Demonym.GABONESE, Continent.AFRICA),
    /** Gambia ({@code GM}). */
    GAMBIA("GM", "Gambia", Demonym.GAMBIAN, Continent.AFRICA),
    /** Georgia ({@code GE}). */
    GEORGIA("GE", "Georgia", Demonym.GEORGIAN, Continent.ASIA),
    /** Germany ({@code DE}). */
    GERMANY("DE", "Germany", Demonym.GERMAN, Continent.EUROPE),
    /** Ghana ({@code GH}). */
    GHANA("GH", "Ghana", Demonym.GHANAIAN, Continent.AFRICA),
    /** Gibraltar ({@code GI}). */
    GIBRALTAR("GI", "Gibraltar", Demonym.GIBRALTARIAN, Continent.EUROPE),
    /** Greece ({@code GR}). */
    GREECE("GR", "Greece", Demonym.GREEK, Continent.EUROPE),
    /** Greenland ({@code GL}). */
    GREENLAND("GL", "Greenland", Demonym.GREENLANDIC, Continent.NORTH_AMERICA),
    /** Grenada ({@code GD}). */
    GRENADA("GD", "Grenada", Demonym.GRENADIAN, Continent.NORTH_AMERICA),
    /** Guadeloupe ({@code GP}). */
    GUADELOUPE("GP", "Guadeloupe", Demonym.GUADELOUPEAN, Continent.NORTH_AMERICA),
    /** Guam ({@code GU}). */
    GUAM("GU", "Guam", Demonym.GUAMANIAN, Continent.OCEANIA),
    /** Guatemala ({@code GT}). */
    GUATEMALA("GT", "Guatemala", Demonym.GUATEMALAN, Continent.NORTH_AMERICA),
    /** Guernsey ({@code GG}). */
    GUERNSEY("GG", "Guernsey", Demonym.UNKNOWN, Continent.EUROPE),
    /** Guinea ({@code GN}). */
    GUINEA("GN", "Guinea", Demonym.GUINEAN, Continent.AFRICA),
    /** Guinea-Bissau ({@code GW}). */
    GUINEA_BISSAU("GW", "Guinea-Bissau", Demonym.BISSAU_GUINEAN, Continent.AFRICA),
    /** Guyana ({@code GY}). */
    GUYANA("GY", "Guyana", Demonym.GUYANESE, Continent.SOUTH_AMERICA),
    /** Haiti ({@code HT}). */
    HAITI("HT", "Haiti", Demonym.HAITIAN, Continent.NORTH_AMERICA),
    /** Heard &amp; McDonald Islands ({@code HM}). */
    HEARD_AND_MCDONALD_ISLANDS("HM", "Heard & McDonald Islands", Demonym.UNKNOWN, Continent.ANTARCTICA),
    /** Honduras ({@code HN}). */
    HONDURAS("HN", "Honduras", Demonym.HONDURAN, Continent.NORTH_AMERICA),
    /** Hong Kong ({@code HK}). */
    HONG_KONG("HK", "Hong Kong", Demonym.HONG_KONGER, Continent.ASIA),
    /** Hungary ({@code HU}). */
    HUNGARY("HU", "Hungary", Demonym.HUNGARIAN, Continent.EUROPE),
    /** Iceland ({@code IS}). */
    ICELAND("IS", "Iceland", Demonym.ICELANDIC, Continent.EUROPE),
    /** India ({@code IN}). */
    INDIA("IN", "India", Demonym.INDIAN, Continent.ASIA),
    /** Indonesia ({@code ID}). */
    INDONESIA("ID", "Indonesia", Demonym.INDONESIAN, Continent.ASIA),
    /** Iran ({@code IR}). */
    IRAN("IR", "Iran", Demonym.IRANIAN, Continent.ASIA),
    /** Iraq ({@code IQ}). */
    IRAQ("IQ", "Iraq", Demonym.IRAQI, Continent.ASIA),
    /** Ireland ({@code IE}). */
    IRELAND("IE", "Ireland", Demonym.IRISH, Continent.EUROPE),
    /** Isle of Man ({@code IM}). */
    ISLE_OF_MAN("IM", "Isle of Man", Demonym.MANX, Continent.EUROPE),
    /** Israel ({@code IL}). */
    ISRAEL("IL", "Israel", Demonym.ISRAELI, Continent.ASIA),
    /** Italy ({@code IT}). */
    ITALY("IT", "Italy", Demonym.ITALIAN, Continent.EUROPE),
    /** Jamaica ({@code JM}). */
    JAMAICA("JM", "Jamaica", Demonym.JAMAICAN, Continent.NORTH_AMERICA),
    /** Japan ({@code JP}). */
    JAPAN("JP", "Japan", Demonym.JAPANESE, Continent.ASIA),
    /** Jersey ({@code JE}). */
    JERSEY("JE", "Jersey", Demonym.UNKNOWN, Continent.EUROPE),
    /** Jordan ({@code JO}). */
    JORDAN("JO", "Jordan", Demonym.JORDANIAN, Continent.ASIA),
    /** Kazakhstan ({@code KZ}). */
    KAZAKHSTAN("KZ", "Kazakhstan", Demonym.KAZAKHSTANI, Continent.ASIA),
    /** Kenya ({@code KE}). */
    KENYA("KE", "Kenya", Demonym.KENYAN, Continent.AFRICA),
    /** Kiribati ({@code KI}). */
    KIRIBATI("KI", "Kiribati", Demonym.I_KIRIBATI, Continent.OCEANIA),
    /** Kuwait ({@code KW}). */
    KUWAIT("KW", "Kuwait", Demonym.KUWAITI, Continent.ASIA),
    /** Kyrgyzstan ({@code KG}). */
    KYRGYZSTAN("KG", "Kyrgyzstan", Demonym.KYRGYZSTANI, Continent.ASIA),
    /** Laos ({@code LA}). */
    LAOS("LA", "Laos", Demonym.LAO, Continent.ASIA),
    /** Latvia ({@code LV}). */
    LATVIA("LV", "Latvia", Demonym.LATVIAN, Continent.EUROPE),
    /** Lebanon ({@code LB}). */
    LEBANON("LB", "Lebanon", Demonym.LEBANESE, Continent.ASIA),
    /** Lesotho ({@code LS}). */
    LESOTHO("LS", "Lesotho", Demonym.BASOTHO, Continent.AFRICA),
    /** Liberia ({@code LR}). */
    LIBERIA("LR", "Liberia", Demonym.LIBERIAN, Continent.AFRICA),
    /** Libya ({@code LY}). */
    LIBYA("LY", "Libya", Demonym.LIBYAN, Continent.AFRICA),
    /** Liechtenstein ({@code LI}). */
    LIECHTENSTEIN("LI", "Liechtenstein", Demonym.LIECHTENSTEINER, Continent.EUROPE),
    /** Lithuania ({@code LT}). */
    LITHUANIA("LT", "Lithuania", Demonym.LITHUANIAN, Continent.EUROPE),
    /** Luxembourg ({@code LU}). */
    LUXEMBOURG("LU", "Luxembourg", Demonym.LUXEMBOURGISH, Continent.EUROPE),
    /** Macao ({@code MO}). */
    MACAO("MO", "Macao", Demonym.MACANESE, Continent.ASIA),
    /** Madagascar ({@code MG}). */
    MADAGASCAR("MG", "Madagascar", Demonym.MALAGASY, Continent.AFRICA),
    /** Malawi ({@code MW}). */
    MALAWI("MW", "Malawi", Demonym.MALAWIAN, Continent.AFRICA),
    /** Malaysia ({@code MY}). */
    MALAYSIA("MY", "Malaysia", Demonym.MALAYSIAN, Continent.ASIA),
    /** Maldives ({@code MV}). */
    MALDIVES("MV", "Maldives", Demonym.MALDIVIAN, Continent.ASIA),
    /** Mali ({@code ML}). */
    MALI("ML", "Mali", Demonym.MALIAN, Continent.AFRICA),
    /** Malta ({@code MT}). */
    MALTA("MT", "Malta", Demonym.MALTESE, Continent.EUROPE),
    /** Marshall Islands ({@code MH}). */
    MARSHALL_ISLANDS("MH", "Marshall Islands", Demonym.MARSHALLESE, Continent.OCEANIA),
    /** Martinique ({@code MQ}). */
    MARTINIQUE("MQ", "Martinique", Demonym.MARTINICAN, Continent.NORTH_AMERICA),
    /** Mauritania ({@code MR}). */
    MAURITANIA("MR", "Mauritania", Demonym.MAURITANIAN, Continent.AFRICA),
    /** Mauritius ({@code MU}). */
    MAURITIUS("MU", "Mauritius", Demonym.MAURITIAN, Continent.AFRICA),
    /** Mayotte ({@code YT}). */
    MAYOTTE("YT", "Mayotte", Demonym.MAHORAN, Continent.AFRICA),
    /** Mexico ({@code MX}). */
    MEXICO("MX", "Mexico", Demonym.MEXICAN, Continent.NORTH_AMERICA),
    /** Micronesia ({@code FM}). */
    MICRONESIA("FM", "Micronesia", Demonym.MICRONESIAN, Continent.OCEANIA),
    /** Moldova ({@code MD}). */
    MOLDOVA("MD", "Moldova", Demonym.MOLDOVAN, Continent.EUROPE),
    /** Monaco ({@code MC}). */
    MONACO("MC", "Monaco", Demonym.MONEGASQUE, Continent.EUROPE),
    /** Mongolia ({@code MN}). */
    MONGOLIA("MN", "Mongolia", Demonym.MONGOLIAN, Continent.ASIA),
    /** Montenegro ({@code ME}). */
    MONTENEGRO("ME", "Montenegro", Demonym.MONTENEGRIN, Continent.EUROPE),
    /** Montserrat ({@code MS}). */
    MONTSERRAT("MS", "Montserrat", Demonym.MONTSERRATIAN, Continent.NORTH_AMERICA),
    /** Morocco ({@code MA}). */
    MOROCCO("MA", "Morocco", Demonym.MOROCCAN, Continent.AFRICA),
    /** Mozambique ({@code MZ}). */
    MOZAMBIQUE("MZ", "Mozambique", Demonym.MOZAMBICAN, Continent.AFRICA),
    /** Myanmar ({@code MM}). */
    MYANMAR("MM", "Myanmar", Demonym.BURMESE, Continent.ASIA),
    /** Namibia ({@code NA}). */
    NAMIBIA("NA", "Namibia", Demonym.NAMIBIAN, Continent.AFRICA),
    /** Nauru ({@code NR}). */
    NAURU("NR", "Nauru", Demonym.NAURUAN, Continent.OCEANIA),
    /** Nepal ({@code NP}). */
    NEPAL("NP", "Nepal", Demonym.NEPALI, Continent.ASIA),
    /** Netherlands ({@code NL}). */
    NETHERLANDS("NL", "Netherlands", Demonym.DUTCH, Continent.EUROPE),
    /** New Caledonia ({@code NC}). */
    NEW_CALEDONIA("NC", "New Caledonia", Demonym.NEW_CALEDONIAN, Continent.OCEANIA),
    /** New Zealand ({@code NZ}). */
    NEW_ZEALAND("NZ", "New Zealand", Demonym.NEW_ZEALANDER, Continent.OCEANIA),
    /** Nicaragua ({@code NI}). */
    NICARAGUA("NI", "Nicaragua", Demonym.NICARAGUAN, Continent.NORTH_AMERICA),
    /** Niger ({@code NE}). */
    NIGER("NE", "Niger", Demonym.NIGERIEN, Continent.AFRICA),
    /** Nigeria ({@code NG}). */
    NIGERIA("NG", "Nigeria", Demonym.NIGERIAN, Continent.AFRICA),
    /** Niue ({@code NU}). */
    NIUE("NU", "Niue", Demonym.NIUEAN, Continent.OCEANIA),
    /** Norfolk Island ({@code NF}). */
    NORFOLK_ISLAND("NF", "Norfolk Island", Demonym.UNKNOWN, Continent.OCEANIA),
    /** Northern Mariana Islands ({@code MP}). */
    NORTHERN_MARIANA_ISLANDS("MP", "Northern Mariana Islands", Demonym.UNKNOWN, Continent.OCEANIA),
    /** North Korea ({@code KP}). */
    NORTH_KOREA("KP", "North Korea", Demonym.NORTH_KOREAN, Continent.ASIA),
    /** North Macedonia ({@code MK}). */
    NORTH_MACEDONIA("MK", "North Macedonia", Demonym.MACEDONIAN, Continent.EUROPE),
    /** Norway ({@code NO}). */
    NORWAY("NO", "Norway", Demonym.NORWEGIAN, Continent.EUROPE),
    /** Oman ({@code OM}). */
    OMAN("OM", "Oman", Demonym.OMANI, Continent.ASIA),
    /** Pakistan ({@code PK}). */
    PAKISTAN("PK", "Pakistan", Demonym.PAKISTANI, Continent.ASIA),
    /** Palau ({@code PW}). */
    PALAU("PW", "Palau", Demonym.PALAUAN, Continent.OCEANIA),
    /** Palestine ({@code PS}). */
    PALESTINE("PS", "Palestine", Demonym.PALESTINIAN, Continent.ASIA),
    /** Panama ({@code PA}). */
    PANAMA("PA", "Panama", Demonym.PANAMANIAN, Continent.NORTH_AMERICA),
    /** Papua New Guinea ({@code PG}). */
    PAPUA_NEW_GUINEA("PG", "Papua New Guinea", Demonym.PAPUA_NEW_GUINEAN, Continent.OCEANIA),
    /** Paraguay ({@code PY}). */
    PARAGUAY("PY", "Paraguay", Demonym.PARAGUAYAN, Continent.SOUTH_AMERICA),
    /** Peru ({@code PE}). */
    PERU("PE", "Peru", Demonym.PERUVIAN, Continent.SOUTH_AMERICA),
    /** Philippines ({@code PH}). */
    PHILIPPINES("PH", "Philippines", Demonym.FILIPINO, Continent.ASIA),
    /** Pitcairn Islands ({@code PN}). */
    PITCAIRN_ISLANDS("PN", "Pitcairn Islands", Demonym.UNKNOWN, Continent.OCEANIA),
    /** Poland ({@code PL}). */
    POLAND("PL", "Poland", Demonym.POLISH, Continent.EUROPE),
    /** Portugal ({@code PT}). */
    PORTUGAL("PT", "Portugal", Demonym.PORTUGUESE, Continent.EUROPE),
    /** Puerto Rico ({@code PR}). */
    PUERTO_RICO("PR", "Puerto Rico", Demonym.PUERTO_RICAN, Continent.NORTH_AMERICA),
    /** Qatar ({@code QA}). */
    QATAR("QA", "Qatar", Demonym.QATARI, Continent.ASIA),
    /** Réunion ({@code RE}). */
    REUNION("RE", "Réunion", Demonym.REUNIONESE, Continent.AFRICA),
    /** Romania ({@code RO}). */
    ROMANIA("RO", "Romania", Demonym.ROMANIAN, Continent.EUROPE),
    /** Russia ({@code RU}). */
    RUSSIA("RU", "Russia", Demonym.RUSSIAN, Continent.EUROPE),
    /** Rwanda ({@code RW}). */
    RWANDA("RW", "Rwanda", Demonym.RWANDAN, Continent.AFRICA),
    /** St. Barthélemy ({@code BL}). */
    SAINT_BARTHELEMY("BL", "St. Barthélemy", Demonym.UNKNOWN, Continent.NORTH_AMERICA),
    /** St. Helena ({@code SH}). */
    SAINT_HELENA("SH", "St. Helena", Demonym.SAINT_HELENIAN, Continent.AFRICA),
    /** St. Kitts &amp; Nevis ({@code KN}). */
    SAINT_KITTS_AND_NEVIS("KN", "St. Kitts & Nevis", Demonym.KITTITIAN, Continent.NORTH_AMERICA),
    /** St. Lucia ({@code LC}). */
    SAINT_LUCIA("LC", "St. Lucia", Demonym.SAINT_LUCIAN, Continent.NORTH_AMERICA),
    /** St. Martin ({@code MF}). */
    SAINT_MARTIN("MF", "St. Martin", Demonym.UNKNOWN, Continent.NORTH_AMERICA),
    /** St. Pierre &amp; Miquelon ({@code PM}). */
    SAINT_PIERRE_AND_MIQUELON("PM", "St. Pierre & Miquelon", Demonym.UNKNOWN, Continent.NORTH_AMERICA),
    /** St. Vincent &amp; Grenadines ({@code VC}). */
    SAINT_VINCENT_AND_GRENADINES("VC", "St. Vincent & Grenadines", Demonym.VINCENTIAN, Continent.NORTH_AMERICA),
    /** Samoa ({@code WS}). */
    SAMOA("WS", "Samoa", Demonym.SAMOAN, Continent.OCEANIA),
    /** San Marino ({@code SM}). */
    SAN_MARINO("SM", "San Marino", Demonym.SAMMARINESE, Continent.EUROPE),
    /** São Tomé &amp; Príncipe ({@code ST}). */
    SAO_TOME_AND_PRINCIPE("ST", "São Tomé & Príncipe", Demonym.SAO_TOMEAN, Continent.AFRICA),
    /** Saudi Arabia ({@code SA}). */
    SAUDI_ARABIA("SA", "Saudi Arabia", Demonym.SAUDI, Continent.ASIA),
    /** Senegal ({@code SN}). */
    SENEGAL("SN", "Senegal", Demonym.SENEGALESE, Continent.AFRICA),
    /** Serbia ({@code RS}). */
    SERBIA("RS", "Serbia", Demonym.SERBIAN, Continent.EUROPE),
    /** Seychelles ({@code SC}). */
    SEYCHELLES("SC", "Seychelles", Demonym.SEYCHELLOIS, Continent.AFRICA),
    /** Sierra Leone ({@code SL}). */
    SIERRA_LEONE("SL", "Sierra Leone", Demonym.SIERRA_LEONEAN, Continent.AFRICA),
    /** Singapore ({@code SG}). */
    SINGAPORE("SG", "Singapore", Demonym.SINGAPOREAN, Continent.ASIA),
    /** Sint Maarten ({@code SX}). */
    SINT_MAARTEN("SX", "Sint Maarten", Demonym.UNKNOWN, Continent.NORTH_AMERICA),
    /** Slovakia ({@code SK}). */
    SLOVAKIA("SK", "Slovakia", Demonym.SLOVAK, Continent.EUROPE),
    /** Slovenia ({@code SI}). */
    SLOVENIA("SI", "Slovenia", Demonym.SLOVENIAN, Continent.EUROPE),
    /** Solomon Islands ({@code SB}). */
    SOLOMON_ISLANDS("SB", "Solomon Islands", Demonym.SOLOMON_ISLANDER, Continent.OCEANIA),
    /** Somalia ({@code SO}). */
    SOMALIA("SO", "Somalia", Demonym.SOMALI, Continent.AFRICA),
    /** South Africa ({@code ZA}). */
    SOUTH_AFRICA("ZA", "South Africa", Demonym.SOUTH_AFRICAN, Continent.AFRICA),
    /** South Georgia &amp; South Sandwich Islands ({@code GS}). */
    SOUTH_GEORGIA_AND_SOUTH_SANDWICH_ISLANDS("GS", "South Georgia & South Sandwich Islands", Demonym.UNKNOWN, Continent.ANTARCTICA),
    /** South Korea ({@code KR}). */
    SOUTH_KOREA("KR", "South Korea", Demonym.SOUTH_KOREAN, Continent.ASIA),
    /** South Sudan ({@code SS}). */
    SOUTH_SUDAN("SS", "South Sudan", Demonym.SOUTH_SUDANESE, Continent.AFRICA),
    /** Spain ({@code ES}). */
    SPAIN("ES", "Spain", Demonym.SPANISH, Continent.EUROPE),
    /** Sri Lanka ({@code LK}). */
    SRI_LANKA("LK", "Sri Lanka", Demonym.SRI_LANKAN, Continent.ASIA),
    /** Sudan ({@code SD}). */
    SUDAN("SD", "Sudan", Demonym.SUDANESE, Continent.AFRICA),
    /** Suriname ({@code SR}). */
    SURINAME("SR", "Suriname", Demonym.SURINAMESE, Continent.SOUTH_AMERICA),
    /** Svalbard &amp; Jan Mayen ({@code SJ}). */
    SVALBARD_AND_JAN_MAYEN("SJ", "Svalbard & Jan Mayen", Demonym.UNKNOWN, Continent.EUROPE),
    /** Sweden ({@code SE}). */
    SWEDEN("SE", "Sweden", Demonym.SWEDISH, Continent.EUROPE),
    /** Switzerland ({@code CH}). */
    SWITZERLAND("CH", "Switzerland", Demonym.SWISS, Continent.EUROPE),
    /** Syria ({@code SY}). */
    SYRIA("SY", "Syria", Demonym.SYRIAN, Continent.ASIA),
    /** Taiwan ({@code TW}). */
    TAIWAN("TW", "Taiwan", Demonym.TAIWANESE, Continent.ASIA),
    /** Tajikistan ({@code TJ}). */
    TAJIKISTAN("TJ", "Tajikistan", Demonym.TAJIKISTANI, Continent.ASIA),
    /** Tanzania ({@code TZ}). */
    TANZANIA("TZ", "Tanzania", Demonym.TANZANIAN, Continent.AFRICA),
    /** Thailand ({@code TH}). */
    THAILAND("TH", "Thailand", Demonym.THAI, Continent.ASIA),
    /** Timor-Leste ({@code TL}). */
    TIMOR_LESTE("TL", "Timor-Leste", Demonym.TIMORESE, Continent.ASIA),
    /** Togo ({@code TG}). */
    TOGO("TG", "Togo", Demonym.TOGOLESE, Continent.AFRICA),
    /** Tokelau ({@code TK}). */
    TOKELAU("TK", "Tokelau", Demonym.TOKELAUAN, Continent.OCEANIA),
    /** Tonga ({@code TO}). */
    TONGA("TO", "Tonga", Demonym.TONGAN, Continent.OCEANIA),
    /** Trinidad &amp; Tobago ({@code TT}). */
    TRINIDAD_AND_TOBAGO("TT", "Trinidad & Tobago", Demonym.TRINIDADIAN, Continent.NORTH_AMERICA),
    /** Tunisia ({@code TN}). */
    TUNISIA("TN", "Tunisia", Demonym.TUNISIAN, Continent.AFRICA),
    /** Türkiye ({@code TR}). */
    TURKEY("TR", "Türkiye", Demonym.TURKISH, Continent.ASIA),
    /** Turkmenistan ({@code TM}). */
    TURKMENISTAN("TM", "Turkmenistan", Demonym.TURKMEN, Continent.ASIA),
    /** Turks &amp; Caicos Islands ({@code TC}). */
    TURKS_AND_CAICOS_ISLANDS("TC", "Turks & Caicos Islands", Demonym.UNKNOWN, Continent.NORTH_AMERICA),
    /** Tuvalu ({@code TV}). */
    TUVALU("TV", "Tuvalu", Demonym.TUVALUAN, Continent.OCEANIA),
    /** Uganda ({@code UG}). */
    UGANDA("UG", "Uganda", Demonym.UGANDAN, Continent.AFRICA),
    /** Ukraine ({@code UA}). */
    UKRAINE("UA", "Ukraine", Demonym.UKRAINIAN, Continent.EUROPE),
    /** United Arab Emirates ({@code AE}). */
    UNITED_ARAB_EMIRATES("AE", "United Arab Emirates", Demonym.EMIRATI, Continent.ASIA),
    /** United Kingdom ({@code GB}). */
    UNITED_KINGDOM("GB", "United Kingdom", Demonym.BRITISH, Continent.EUROPE),
    /** United States ({@code US}). */
    UNITED_STATES("US", "United States", Demonym.AMERICAN, Continent.NORTH_AMERICA),
    /** Uruguay ({@code UY}). */
    URUGUAY("UY", "Uruguay", Demonym.URUGUAYAN, Continent.SOUTH_AMERICA),
    /** U.S. Outlying Islands ({@code UM}). */
    US_OUTLYING_ISLANDS("UM", "U.S. Outlying Islands", Demonym.UNKNOWN, Continent.OCEANIA),
    /** U.S. Virgin Islands ({@code VI}). */
    US_VIRGIN_ISLANDS("VI", "U.S. Virgin Islands", Demonym.VIRGIN_ISLANDER, Continent.NORTH_AMERICA),
    /** Uzbekistan ({@code UZ}). */
    UZBEKISTAN("UZ", "Uzbekistan", Demonym.UZBEKISTANI, Continent.ASIA),
    /** Vanuatu ({@code VU}). */
    VANUATU("VU", "Vanuatu", Demonym.NI_VANUATU, Continent.OCEANIA),
    /** Vatican City ({@code VA}). */
    VATICAN_CITY("VA", "Vatican City", Demonym.VATICAN, Continent.EUROPE),
    /** Venezuela ({@code VE}). */
    VENEZUELA("VE", "Venezuela", Demonym.VENEZUELAN, Continent.SOUTH_AMERICA),
    /** Vietnam ({@code VN}). */
    VIETNAM("VN", "Vietnam", Demonym.VIETNAMESE, Continent.ASIA),
    /** Wallis &amp; Futuna ({@code WF}). */
    WALLIS_AND_FUTUNA("WF", "Wallis & Futuna", Demonym.UNKNOWN, Continent.OCEANIA),
    /** Western Sahara ({@code EH}). */
    WESTERN_SAHARA("EH", "Western Sahara", Demonym.SAHRAWI, Continent.AFRICA),
    /** Yemen ({@code YE}). */
    YEMEN("YE", "Yemen", Demonym.YEMENI, Continent.ASIA),
    /** Zambia ({@code ZM}). */
    ZAMBIA("ZM", "Zambia", Demonym.ZAMBIAN, Continent.AFRICA),
    /** Zimbabwe ({@code ZW}). */
    ZIMBABWE("ZW", "Zimbabwe", Demonym.ZIMBABWEAN, Continent.AFRICA),

    /** Not known, or not one of the others. */
    UNKNOWN("XX", "Unknown", Demonym.UNKNOWN, Continent.UNKNOWN);

    /** {@return whether this is {@link #UNKNOWN}} */
    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    private final String code;
    private final String displayName;
    private final Demonym demonym;
    private final Continent continent;

    Country(String code, String displayName, Demonym demonym, Continent continent) {
        this.code = code;
        this.displayName = displayName;
        this.demonym = demonym;
        this.continent = continent;
    }

    // ========================= Accessors =========================

    /** {@return its ISO 3166-1 alpha-2 code, upper case, e.g. {@code "PH"}; {@code "XX"} for {@link #UNKNOWN}} */
    public String code() {
        return code;
    }

    /** {@return its common English name, e.g. {@code "Philippines"}} */
    public String displayName() {
        return displayName;
    }

    /**
     * Returns the primary {@link Demonym} for this country.
     *
     * @return the demonym value object
     */
    public Demonym demonym() {
        return demonym;
    }

    /** {@return the continent it is on} */
    public Continent continent() {
        return continent;
    }

    /**
     * Returns its flag emoji, made from the two regional indicator symbols (U+1F1E6 to U+1F1FF) for its code.
     * {@link #UNKNOWN} gives the symbols for {@code XX}, which is not a flag.
     *
     * @return the flag emoji, e.g. the Philippine flag for {@code PH}
     */
    public String flagEmoji() {
        if (code.length() != 2) {
            return "";
        }
        int firstChar = code.charAt(0) - 'A' + 0x1F1E6;
        int secondChar = code.charAt(1) - 'A' + 0x1F1E6;
        return new String(Character.toChars(firstChar)) + new String(Character.toChars(secondChar));
    }

    // ========================= Lookup Methods =========================

    /**
     * Finds a country by its code, enum name, display name or any form of its {@link Demonym}, ignoring case
     * and surrounding whitespace. A demonym shared by several countries gives the first one declared.
     *
     * @param value e.g. {@code "PH"}, {@code "PHILIPPINES"}, {@code "Philippines"}, {@code "Filipino"}
     * @return the country, or {@code null} when nothing matches or {@code value} is {@code null} or blank
     */
    public static @Nullable Country fromAny(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return Arrays.stream(values())
                .filter(country ->
                        country.code.equalsIgnoreCase(trimmed)
                                || country.name().equalsIgnoreCase(trimmed)
                                || country.displayName.equalsIgnoreCase(trimmed)
                                || country.demonym.matches(trimmed)
                )
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds a country by its ISO code or enum name, ignoring case.
     *
     * @param code e.g. {@code "ph"} or {@code "PHILIPPINES"}
     * @return the country, or {@code null} when none matches
     */
    public static @Nullable Country fromCode(String code) {
        return Arrays.stream(values())
                .filter(country -> country.code.equalsIgnoreCase(code) || country.name().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds a country by its display name, ignoring case.
     *
     * @param name e.g. {@code "philippines"}
     * @return the country, or empty when none has that name
     */
    public static Optional<Country> fromName(String name) {
        return Arrays.stream(values())
                .filter(country -> country.displayName.equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Returns every country on a continent, in declaration order.
     *
     * @param continent the continent
     * @return the countries on it
     */
    public static List<Country> byContinent(Continent continent) {
        return Arrays.stream(values())
                .filter(country -> country.continent == continent)
                .toList();
    }

    /**
     * Returns every country whose primary demonym is the given one, in declaration order.
     *
     * @param demonym the demonym
     * @return the countries with it
     */
    public static List<Country> byDemonym(Demonym demonym) {
        return Arrays.stream(values())
                .filter(country -> country.demonym == demonym)
                .toList();
    }

    // ========================= Comparison =========================

    /**
     * Compares countries by display name alphabetically, ignoring case.
     *
     * @param other the Country to compare to
     * @return negative if this comes before, zero if equal, positive if after
     */
    public int compareByName(Country other) {
        return this.displayName.compareToIgnoreCase(other.displayName);
    }

    /**
     * Compares countries by ISO code alphabetically.
     *
     * @param other the Country to compare to
     * @return negative if this comes before, zero if equal, positive if after
     */
    public int compareByCode(Country other) {
        return this.code.compareTo(other.code);
    }


    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Parses a country from its code, enum name, display name or demonym, as {@link #fromAny(String)} does.
     *
     * @param text the code, name or demonym, e.g. {@code "PH"}, {@code "Philippines"}
     * @return the country
     * @throws ValueObjectException if it is {@code null} or matches no country other than {@link #UNKNOWN}
     */
    public static Country parse(String text) {
        if (text == null) throw new ValueObjectException("A Country is required");
        Country c = fromAny(text);
        if (c == null || c == UNKNOWN) throw new ValueObjectException("Not a country: '" + text + "'");
        return c;
    }

    /**
     * Parses a country as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as a {@link Country}, or empty when it is not one; never throws
     */
    public static Optional<Country> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text names a country, as {@link #parse(String)} reads it.
     *
     * @param text any text
     * @return whether it is a {@link Country}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
