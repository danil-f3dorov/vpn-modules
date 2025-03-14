package common.util.validate


object ValidateUtil {

    fun validateIfCityExist(country: String, city: String): String {
        return if (city != "") {
            "$country, $city"
        } else {
            country
        }
    }
}