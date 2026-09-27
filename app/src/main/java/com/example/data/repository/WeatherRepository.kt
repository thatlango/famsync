package com.example.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class WeatherInfo(
    val cityName: String,
    val temperatureC: Float,
    val temperatureF: Int,
    val condition: String,
    val iconEmoji: String,
    val highF: Int,
    val lowF: Int,
    val humidityPercent: Int,
    val windSpeedMph: Int,
    val dailyForecast: List<DailyWeather>
)

data class DailyWeather(
    val dayName: String,
    val iconEmoji: String,
    val highF: Int,
    val lowF: Int
)

data class CityLocation(
    val name: String,
    val lat: Double,
    val lon: Double
)

class WeatherRepository {

    val availableCities = listOf(
        CityLocation("San Francisco", 37.7749, -122.4194),
        CityLocation("New York", 40.7128, -74.0060),
        CityLocation("Chicago", 41.8781, -87.6298),
        CityLocation("Austin", 30.2672, -97.7431),
        CityLocation("Los Angeles", 34.0522, -118.2437),
        CityLocation("London", 51.5074, -0.1278),
        CityLocation("Paris", 48.8566, 2.3522),
        CityLocation("Tokyo", 35.6762, 139.6503),
        CityLocation("Sydney", -33.8688, 151.2093)
    )

    private val _weatherState = MutableStateFlow(getFallbackWeather("San Francisco"))
    val weatherState: StateFlow<WeatherInfo> = _weatherState

    private val _selectedCity = MutableStateFlow(availableCities[0])
    val selectedCity: StateFlow<CityLocation> = _selectedCity

    suspend fun searchCities(query: String): List<CityLocation> {
        if (query.length < 2) return emptyList()
        return withContext(Dispatchers.IO) {
            try {
                val urlString = "https://geocoding-api.open-meteo.com/v1/search?name=${java.net.URLEncoder.encode(query, "UTF-8")}&count=10&language=en&format=json"
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                if (conn.responseCode == 200) {
                    val stream = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(stream)
                    val results = json.optJSONArray("results") ?: return@withContext emptyList()
                    val list = mutableListOf<CityLocation>()
                    for (i in 0 until results.length()) {
                        val obj = results.getJSONObject(i)
                        val name = obj.getString("name")
                        val admin = obj.optString("admin1", "")
                        val country = obj.optString("country", "")
                        val fullName = listOf(name, admin, country).filter { it.isNotBlank() }.joinToString(", ")
                        list.add(
                            CityLocation(
                                fullName,
                                obj.getDouble("latitude"),
                                obj.getDouble("longitude")
                            )
                        )
                    }
                    list
                } else emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun selectCity(city: CityLocation) {
        _selectedCity.value = city
        fetchWeather(city)
    }

    suspend fun detectCityFromLocation(lat: Double, lon: Double): CityLocation {
        return withContext(Dispatchers.IO) {
            // Find closest from available for now, or just return as detected
            val closest = availableCities.minByOrNull {
                val dLat = it.lat - lat
                val dLon = it.lon - lon
                dLat * dLat + dLon * dLon
            }
            closest ?: CityLocation("Detected Location", lat, lon)
        }
    }

    suspend fun fetchWeather(city: CityLocation = _selectedCity.value) {
        withContext(Dispatchers.IO) {
            try {
                val urlString = "https://api.open-meteo.com/v1/forecast?latitude=${city.lat}&longitude=${city.lon}&current_weather=true&daily=weathercode,temperature_2m_max,temperature_2m_min&timezone=auto"
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 4000
                conn.readTimeout = 4000

                if (conn.responseCode == 200) {
                    val stream = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(stream)

                    val currentWeather = json.getJSONObject("current_weather")
                    val tempC = currentWeather.getDouble("temperature").toFloat()
                    val tempF = (tempC * 9 / 5 + 32).toInt()
                    val weatherCode = currentWeather.getInt("weathercode")
                    val windSpeedKmh = currentWeather.getDouble("windspeed")
                    val windMph = (windSpeedKmh * 0.621371).toInt()

                    val daily = json.getJSONObject("daily")
                    val maxTemps = daily.getJSONArray("temperature_2m_max")
                    val minTemps = daily.getJSONArray("temperature_2m_min")
                    val codes = daily.getJSONArray("weathercode")

                    val highC = if (maxTemps.length() > 0) maxTemps.getDouble(0) else tempC + 3.0
                    val lowC = if (minTemps.length() > 0) minTemps.getDouble(0) else tempC - 4.0
                    val highF = (highC * 9 / 5 + 32).toInt()
                    val lowF = (lowC * 9 / 5 + 32).toInt()

                    val (conditionStr, emoji) = decodeWeatherCode(weatherCode)

                    val dayNames = listOf("Today", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    val dailyList = mutableListOf<DailyWeather>()
                    val numDays = minOf(5, codes.length())
                    for (i in 0 until numDays) {
                        val codeI = codes.getInt(i)
                        val maxI = (maxTemps.getDouble(i) * 9 / 5 + 32).toInt()
                        val minI = (minTemps.getDouble(i) * 9 / 5 + 32).toInt()
                        val (_, em) = decodeWeatherCode(codeI)
                        dailyList.add(
                            DailyWeather(
                                dayName = if (i == 0) "Today" else dayNames[i % dayNames.size],
                                iconEmoji = em,
                                highF = maxI,
                                lowF = minI
                            )
                        )
                    }

                    val info = WeatherInfo(
                        cityName = city.name,
                        temperatureC = tempC,
                        temperatureF = tempF,
                        condition = conditionStr,
                        iconEmoji = emoji,
                        highF = highF,
                        lowF = lowF,
                        humidityPercent = 65,
                        windSpeedMph = windMph,
                        dailyForecast = dailyList
                    )
                    _weatherState.value = info
                } else {
                    _weatherState.value = getFallbackWeather(city.name)
                }
            } catch (e: Exception) {
                _weatherState.value = getFallbackWeather(city.name)
            }
        }
    }

    private fun decodeWeatherCode(code: Int): Pair<String, String> {
        return when (code) {
            0 -> "Clear Sky" to "☀️"
            1, 2, 3 -> "Partly Cloudy" to "⛅"
            45, 48 -> "Foggy" to "🌫️"
            51, 53, 55 -> "Light Drizzle" to "🌧️"
            61, 63, 65 -> "Rainy" to "🌧️"
            71, 73, 75 -> "Snowy" to "❄️"
            80, 81, 82 -> "Rain Showers" to "🌦️"
            95, 96, 99 -> "Thunderstorm" to "🌩️"
            else -> "Sunny" to "☀️"
        }
    }

    private fun getFallbackWeather(cityName: String): WeatherInfo {
        return WeatherInfo(
            cityName = cityName,
            temperatureC = 22.0f,
            temperatureF = 72,
            condition = "Partly Sunny",
            iconEmoji = "⛅",
            highF = 76,
            lowF = 62,
            humidityPercent = 58,
            windSpeedMph = 8,
            dailyForecast = listOf(
                DailyWeather("Today", "⛅", 76, 62),
                DailyWeather("Tomorrow", "☀️", 78, 64),
                DailyWeather("Wed", "🌦️", 71, 59),
                DailyWeather("Thu", "☀️", 75, 61),
                DailyWeather("Fri", "🌤️", 79, 65)
            )
        )
    }
}
