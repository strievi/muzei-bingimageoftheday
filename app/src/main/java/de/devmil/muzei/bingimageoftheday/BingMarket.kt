/*
 * Copyright 2014 Devmil Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.devmil.muzei.bingimageoftheday

/**
 * Created by devmil on 16.02.14.

 * This enum represents all the known Bing markets.
 * It contains the market code (used by Bing), a name (for selection in the UI) and
 * a drawable resource containing the flag
 */
enum class BingMarket private constructor(val marketCode: String, private val marketResourceId: Int, val logoResourceId: Int) {
    Unknown("", R.string.mkt_unknown, R.drawable.unknown),
    AR_SA("ar-SA", R.string.mkt_ar_SA, R.drawable.saudi_arabia),
    AR_XA("ar-XA", R.string.mkt_ar_XA, R.drawable.united_arab_emirates),
    BG_BG("bg-BG", R.string.mkt_bg_BG, R.drawable.bulgaria),
    CS_CZ("cs-CZ", R.string.mkt_cs_CZ, R.drawable.czech_republic),
    DA_DK("da-DK", R.string.mkt_da_DK, R.drawable.denmark),
    DE_AT("de-AT", R.string.mkt_de_AT, R.drawable.austria),
    DE_CH("de-CH", R.string.mkt_de_CH, R.drawable.switzerland),
    DE_DE("de-DE", R.string.mkt_de_DE, R.drawable.germany),
    EL_GR("el-GR", R.string.mkt_el_GR, R.drawable.greece),
    EN_AU("en-AU", R.string.mkt_en_AU, R.drawable.australia),
    EN_CA("en-CA", R.string.mkt_en_CA, R.drawable.canada),
    EN_GB("en-GB", R.string.mkt_en_GB, R.drawable.united_kingdom),
    EN_ID("en-ID", R.string.mkt_en_ID, R.drawable.indonesia),
    EN_IE("en-IE", R.string.mkt_en_IE, R.drawable.ireland),
    EN_IN("en-IN", R.string.mkt_en_IN, R.drawable.india),
    EN_MY("en-MY", R.string.mkt_en_MY, R.drawable.malaysia),
    EN_NZ("en-NZ", R.string.mkt_en_NZ, R.drawable.new_zealand),
    EN_PH("en-PH", R.string.mkt_en_PH, R.drawable.philippines),
    EN_SG("en-SG", R.string.mkt_en_SG, R.drawable.singapore),
    EN_US("en-US", R.string.mkt_en_US, R.drawable.united_states),
    EN_XA("en-XA", R.string.mkt_en_XA, R.drawable.united_arab_emirates),
    EN_ZA("en-ZA", R.string.mkt_en_ZA, R.drawable.south_africa),
    ES_AR("es-AR", R.string.mkt_es_AR, R.drawable.argentina),
    ES_CL("es-CL", R.string.mkt_es_CL, R.drawable.chile),
    ES_ES("es-ES", R.string.mkt_es_ES, R.drawable.spain),
    ES_MX("es-MX", R.string.mkt_es_MX, R.drawable.mexico),
    ES_US("es-US", R.string.mkt_es_US, R.drawable.united_states),
    ES_XL("es-XL", R.string.mkt_es_XL, R.drawable.latin_america),
    ET_EE("et-EE", R.string.mkt_et_EE, R.drawable.estonia),
    FI_FI("fi-FI", R.string.mkt_fi_FI, R.drawable.finland),
    FR_BE("fr-BE", R.string.mkt_fr_BE, R.drawable.belgium),
    FR_CA("fr-CA", R.string.mkt_fr_CA, R.drawable.canada),
    FR_CH("fr-CH", R.string.mkt_fr_CH, R.drawable.switzerland),
    FR_FR("fr-FR", R.string.mkt_fr_FR, R.drawable.france),
    HE_IL("he-IL", R.string.mkt_he_IL, R.drawable.israel),
    HR_HR("hr-HR", R.string.mkt_hr_HR, R.drawable.croatia),
    HU_HU("hu-HU", R.string.mkt_hu_HU, R.drawable.hungary),
    IT_IT("it-IT", R.string.mkt_it_IT, R.drawable.italy),
    JA_JP("ja-JP", R.string.mkt_ja_JP, R.drawable.japan),
    KO_KR("ko-KR", R.string.mkt_ko_KR, R.drawable.south_korea),
    LT_LT("lt-LT", R.string.mkt_lt_LT, R.drawable.lithuania),
    LV_LV("lv-LV", R.string.mkt_lv_LV, R.drawable.latvia),
    NB_NO("nb-NO", R.string.mkt_nb_NO, R.drawable.norway),
    NL_BE("nl-BE", R.string.mkt_nl_BE, R.drawable.belgium),
    NL_NL("nl-NL", R.string.mkt_nl_NL, R.drawable.netherlands),
    PL_PL("pl-PL", R.string.mkt_pl_PL, R.drawable.poland),
    PT_BR("pt-BR", R.string.mkt_pt_BR, R.drawable.brazil),
    PT_PT("pt-PT", R.string.mkt_pt_PT, R.drawable.portugal),
    RO_RO("ro-RO", R.string.mkt_ro_RO, R.drawable.romania),
    RU_RU("ru-RU", R.string.mkt_ru_RU, R.drawable.russia),
    SK_SK("sk-SK", R.string.mkt_sk_SK, R.drawable.slovakia),
    SV_SE("sv-SE", R.string.mkt_sv_SE, R.drawable.sweden),
    TH_TH("th-TH", R.string.mkt_th_TH, R.drawable.thailand),
    TR_TR("tr-TR", R.string.mkt_tr_TR, R.drawable.turkey),
    UK_UA("uk-UA", R.string.mkt_uk_UA, R.drawable.ukraine),
    ZH_CN("zh-CN", R.string.mkt_zh_CN, R.drawable.china),
    ZH_HK("zh-HK", R.string.mkt_zh_HK, R.drawable.hong_kong),
    ZH_TW("zh-TW", R.string.mkt_zh_TW, R.drawable.taiwan);

    override fun toString(): String {
        return BingImageOfTheDayApplication.instance.getString(marketResourceId)
    }

    companion object {

        fun fromMarketCode(marketCode: String): BingMarket {
            return values().firstOrNull { it.marketCode == marketCode } ?: Unknown
        }

        fun selectableValues(): Array<BingMarket> {
            return values().copyOfRange(1, values().size - 1);
        }
    }

}
