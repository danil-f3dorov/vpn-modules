package common.util.parse

import com.common.R
import data.room.entity.Server

object ParseFlag {
    fun findFlagForServer(server: Server?): Int {
        return when (server?.short) {
            "KR" -> R.drawable.ic_south_korea
            "US" -> R.drawable.ic_united_states
            "AU" -> R.drawable.ic_australia
            "JP" -> R.drawable.ic_japan
            "VN" -> R.drawable.ic_vietnam
            "TH" -> R.drawable.ic_thailand
            "RU" -> R.drawable.ic_russia
            "IN" -> R.drawable.ic_india
            "ID" -> R.drawable.ic_indonesia
            "HU" -> R.drawable.ic_hungary
            "BR" -> R.drawable.ic_brazil
            "EU" -> R.drawable.ic_european_union
            "GE" -> R.drawable.ic_georgia
            "SC" -> R.drawable.ic_seychelles
            "MN" -> R.drawable.ic_mongolia
            "CA" -> R.drawable.ic_canada
            "HK" -> R.drawable.ic_hong_kong
            "CH" -> R.drawable.ic_switzerland
            "BY" -> R.drawable.ic_belarus
            "TR" -> R.drawable.ic_turkey
            "BE" -> R.drawable.ic_belgium
            "DE" -> R.drawable.ic_germany
            "IL" -> R.drawable.ic_israel
            "FR" -> R.drawable.ic_france
            "ES" -> R.drawable.ic_spain
            "IR" -> R.drawable.ic_iran
            "PE" -> R.drawable.ic_peru
            "FI" -> R.drawable.ic_finland
            "CO" -> R.drawable.ic_colombia
            "AR" -> R.drawable.ic_argentina
            "PL" -> R.drawable.ic_poland
            "NO" -> R.drawable.ic_norway
            "GD" -> R.drawable.ic_grenada
            "CN" -> R.drawable.ic_china
            "LR" -> R.drawable.ic_liberia
            "HT" -> R.drawable.ic_haiti
            "GG" -> R.drawable.ic_guernsey
            "BQ" -> R.drawable.ic_bonaire
            "ME" -> R.drawable.ic_montenegro
            "NG" -> R.drawable.ic_nigeria
            "VU" -> R.drawable.ic_vanuatu
            "CM" -> R.drawable.ic_cameroon
            "LC" -> R.drawable.ic_st_lucia
            "DM" -> R.drawable.ic_dominica
            "MO" -> R.drawable.ic_macao
            "MQ" -> R.drawable.ic_martinique
            "RW" -> R.drawable.ic_rwanda
            "QA" -> R.drawable.ic_qatar
            "MK" -> R.drawable.ic_republic_of_macedonia
            "MA" -> R.drawable.ic_morocco
            "HR" -> R.drawable.ic_croatia
            "PG" -> R.drawable.ic_papua_new_guinea
            "MT" -> R.drawable.ic_malta
            "NF" -> R.drawable.ic_norfolk_island
            "KN" -> R.drawable.ic_cambodia
            "EG" -> R.drawable.ic_egypt
            "CG" -> R.drawable.ic_republic_of_the_congo
            "CR" -> R.drawable.ic_costa_rica
            "SZ" -> R.drawable.ic_swaziland
            "KG" -> R.drawable.ic_kyrgyzstan
            "AE" -> R.drawable.ic_united_arab_emirates
            "BO" -> R.drawable.ic_bolivia
            "CZ" -> R.drawable.ic_czech_republic
            "KE" -> R.drawable.ic_kenya
            "LA" -> R.drawable.ic_laos
            "PH" -> R.drawable.ic_philippines
            "UA" -> R.drawable.ic_ukraine
            "UZ" -> R.drawable.ic_uzbekistan
            "MX" -> R.drawable.ic_mexico
            "NZ" -> R.drawable.ic_new_zealand
            "KH" -> R.drawable.ic_cambodia
            "PA" -> R.drawable.ic_panama
            "IT" -> R.drawable.ic_italy
            "GB" -> R.drawable.ic_united_kingdom
            "GR" -> R.drawable.ic_greece
            "SA" -> R.drawable.ic_saudi_arabia
            "ZA" -> R.drawable.ic_south_africa
            "SG" -> R.drawable.ic_singapore
            "MY" -> R.drawable.ic_malaysia
            "DK" -> R.drawable.ic_denmark
            "SE" -> R.drawable.ic_sweden
            "PT" -> R.drawable.ic_portugal
            "NL" -> R.drawable.ic_netherlands
            "AT" -> R.drawable.ic_austria
            "BD" -> R.drawable.ic_bangladesh
            "PK" -> R.drawable.ic_pakistan
            "RS" -> R.drawable.ic_serbia
            "RO" -> R.drawable.ic_romania
            "JO" -> R.drawable.ic_jordan
            "TN" -> R.drawable.ic_tunisia
            "KZ" -> R.drawable.ic_kazakhstan
            "CL" -> R.drawable.ic_chile
            "IQ" -> R.drawable.ic_iraq
            "SN" -> R.drawable.ic_senegal
            "DZ" -> R.drawable.ic_algeria
            "ET" -> R.drawable.ic_ethiopia
            "UG" -> R.drawable.ic_uganda
            "NI" -> R.drawable.ic_nicaragua
            "HN" -> R.drawable.ic_honduras
            "YE" -> R.drawable.ic_yemen
            "CU" -> R.drawable.ic_cuba
            "JM" -> R.drawable.ic_jamaica
            "GT" -> R.drawable.ic_guatemala
            "TT" -> R.drawable.ic_trinidad_and_tobago
            "DO" -> R.drawable.ic_dominican_republic
            "EE" -> R.drawable.ic_estonia
            "LT" -> R.drawable.ic_lithuania
            "LV" -> R.drawable.ic_latvia
            "CY" -> R.drawable.ic_cyprus
            "LK" -> R.drawable.ic_sri_lanka
            "AM" -> R.drawable.ic_armenia
            "OM" -> R.drawable.ic_oman
            "BA" -> R.drawable.ic_bosnia_and_herzegovina
            "BB" -> R.drawable.ic_barbados
            "GQ" -> R.drawable.ic_equatorial_guinea
            "BG" -> R.drawable.ic_bulgaria
            "BT" -> R.drawable.ic_bhutan
            "GH" -> R.drawable.ic_ghana
            "IS" -> R.drawable.ic_iceland
            "ZM" -> R.drawable.ic_zambia
            "TV" -> R.drawable.ic_tuvalu
            "WS" -> R.drawable.ic_samoa
            "CD" -> R.drawable.ic_democratic_republic_of_congo
            "SO" -> R.drawable.ic_somaliland
            "SS" -> R.drawable.ic_south_sudan
            "MG" -> R.drawable.ic_madagascar
            "VC" -> R.drawable.ic_st_vincent_and_the_grenadines
            "TK" -> R.drawable.ic_tokelau
            "MD" -> R.drawable.ic_transnistria
            "LY" -> R.drawable.ic_libya
            "TM" -> R.drawable.ic_turkmenistan
            "KI" -> R.drawable.ic_kiribati
            "MV" -> R.drawable.ic_maldives
            "TC" -> R.drawable.ic_turks_and_caicos
            "NE" -> R.drawable.ic_niger
            "IM" -> R.drawable.ic_isle_of_man
            "VG" -> R.drawable.ic_virgin_islands
            "FK" -> R.drawable.ic_falkland_islands
            "MW" -> R.drawable.ic_malawi
            "GI" -> R.drawable.ic_gibraltar
            "MZ" -> R.drawable.ic_moldova
            "PS" -> R.drawable.ic_palestine
            "GU" -> R.drawable.ic_guam
            "EH" -> R.drawable.ic_sahrawi_arab_democratic_republic
            "TW" -> R.drawable.ic_taiwan
            "SD" -> R.drawable.ic_sudan
            "TJ" -> R.drawable.ic_tajikistan
            "NR" -> R.drawable.ic_nauru
            "TO" -> R.drawable.ic_tonga
            "NU" -> R.drawable.ic_niue
            "PF" -> R.drawable.ic_french_polynesia
            "IE" -> R.drawable.ic_ireland
            "PW" -> R.drawable.ic_palau
            "VE" -> R.drawable.ic_venezuela

            else -> R.drawable.ic_cross
        }

    }
}