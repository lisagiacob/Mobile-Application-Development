package com.example.lab2

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DBSeeder {

    private val db = FirebaseFirestore.getInstance()
    val mockRef = FirebaseFirestore.getInstance().collection("users").document("2")

    private val StaticTravels: Map<String, Travel> = mapOf(
        "101" to Travel(
            id = "101",
            title = "Norway on the Road",
            owner = "2NBchwEdVBXf4C2AYHkFUMvdyk82" to "John01",
            description = "travel 1 description",
            dateRange = Pair(timestampFromDateString("03/05/2025")!!, timestampFromDateString("07/05/2025")!!),
            ageRange = "25 - 35",
            price = "400",
            groupSize = "5 - 10",
            participants = linkedMapOf(
                "user2" to Pair(Participant("user2", "bobbyB", listOf(AdditionalParticipant("Joe", "Smith", "06/01/2000", "+391234567898"))), true),
                "user5" to Pair(Participant("user5", "eveF", null), true),
                "user3" to Pair(Participant("user3", "charlie", null), false)
            ),
            locations = mutableListOf(
                Triple("03/05/2025", "Oslo", true),
                Triple("05/05/2025", "Bergen", true),
                Triple("07/05/2025", "Stavanger", false)
            ),
            referencedTravel = null,
            images = mutableListOf(
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/northern_lights.jpg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvbm9ydGhlcm5fbGlnaHRzLmpwZyIsImlhdCI6MTc0ODE4MTg0MywiZXhwIjoxNzc5NzE3ODQzfQ.tcuBISxnezRml8QN6pFhcPlF2yBTzD6Z7CkRjJsILhY"),
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/norway1.jpg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvbm9yd2F5MS5qcGciLCJpYXQiOjE3NDgxODE4NTMsImV4cCI6MTc3OTcxNzg1M30.IEQMjLRWsik1SdOksR3P9t6AbOYiRz6gsnKu_GMJVYU"),
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/norway2.jpg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvbm9yd2F5Mi5qcGciLCJpYXQiOjE3NDgxODE4NjMsImV4cCI6MTc3OTcxNzg2M30.-RmginM64rOmcPk7YsPOOqIH1qTomoM74uTv3u4wDcQ")
            ),
            tags = mutableListOf("Adventure", "Relax", "Nature", "Exploring"),
            itinerary = mutableListOf("Itinerary day 1", "Itinerary day 2", "Itinerary day 3"),
            activities = mutableListOf("Activity 1" to true, "Activity 2" to false, "Activity 3" to true),
            reviews = mutableListOf(),
            questions = mutableListOf("Question 1", "Question 2"),
            answers = mutableListOf(Pair(0, "Answer 1"), Pair(0, "Answer 2")),
            pendingApplications = mutableListOf(
                Participant("user1", "alice123", listOf(AdditionalParticipant("Joe", "Smith", "06/01/2000", "+391234567898"))),
                Participant("user4", "danaE", listOf(AdditionalParticipant("Joe", "Smith", "06/01/2000", "+391234567898")))
            )
        ),
        "10" to Travel(
            id = "10", title = "Japan Cultural Tour", owner = "user1" to "alice123", description = "Explore Japan’s temples and traditions.",
            dateRange = Pair(timestampFromDateString("01/10/2025")!!, timestampFromDateString("12/10/2025")!!), ageRange = "18 - 40", price = "1200", groupSize = "6 - 12",
            participants = linkedMapOf(
                "user2" to Pair(Participant("user2", "bobbyB", null), true),
                "user5" to Pair(Participant("user5", "eveF", null), true),
                "user3" to Pair(Participant("user3", "charlie", null), true)),
            locations = mutableListOf(Triple("01/10/2025", "Tokyo", true), Triple("05/10/2025", "Kyoto", true), Triple("09/10/2025", "Osaka", false)),
            referencedTravel = null, images = mutableListOf(
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/cherry_blossom.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvY2hlcnJ5X2Jsb3Nzb20uanBlZyIsImlhdCI6MTc0ODE4MTc2MCwiZXhwIjoxNzc5NzE3NzYwfQ.rKR09rsDJ8zdm9HSeF_kfHgWLRh8NSxeAZWtCI4Aip4"),
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/japan1.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvamFwYW4xLmpwZWciLCJpYXQiOjE3NDgxODE3NzcsImV4cCI6MTc3OTcxNzc3N30.4kt2EUf14CUCsc37t3QEeOTBJPVgcZIq55wYGBE8d0Q"),
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/temple.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvdGVtcGxlLmpwZWciLCJpYXQiOjE3NDgxODE3ODksImV4cCI6MTc3OTcxNzc4OX0.A4SbTz9N5HKoy0SK_5gYuyLdqhJnSDJGmhEivFTXqYk")
                /*TravelImage.Resource(R.drawable.japan1),
                TravelImage.Resource(R.drawable.temple),
                TravelImage.Resource(R.drawable.cherry_blossom)*/
            ),
            tags = mutableListOf("Culture", "Food", "Tradition"), itinerary = mutableListOf("Tea ceremony", "Shrine visits", "City tour"),
            activities = mutableListOf("Cultural workshops" to true, "Shopping" to true, "Hiking" to false), reviews = mutableListOf(),
            questions = mutableListOf("Is food included?"), answers = mutableListOf(Pair(1, "Breakfast and 2 dinners."))
        ),
        "3" to Travel(
            id = "3", title = "Greek Island Hopping", owner = "user2" to "bobbyB", description = "Explore the Greek islands by ferry.",
            dateRange = Pair(timestampFromDateString("10/06/2024")!!, timestampFromDateString("18/06/2024")!!), ageRange = "20 - 30", price = "600", groupSize = "6 - 12",
            participants = linkedMapOf(
                //"user2" to Pair(Participant("user2", "bobbyB", null), true),
                "user5" to Pair(Participant("user5", "eveF", null), true),
                "1" to Pair(Participant("1", "John01", null), true),
                "user6" to Pair(Participant("user6", "frankG", null), true)

            ),
            locations = mutableListOf(Triple("10/06/2025", "Santorini", true), Triple("13/06/2025", "Mykonos", true), Triple("16/06/2025", "Paros", false)),
            referencedTravel = null, images = mutableListOf(
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/beach.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvYmVhY2guanBlZyIsImlhdCI6MTc0ODE4MTg5NywiZXhwIjoxNzc5NzE3ODk3fQ.-n0-r7Ta4GAVq6ngLZM_1zAJ6n2Nk9Jj7sXnTjzkAnE"),
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/greece1.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvZ3JlZWNlMS5qcGVnIiwiaWF0IjoxNzQ4MTgxOTExLCJleHAiOjE3Nzk3MTc5MTF9.6EA9FzomwvZ3V-NCnfMNhpmkco3A1Lqsg8NAte3D2Ok"),
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/greece2.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvZ3JlZWNlMi5qcGVnIiwiaWF0IjoxNzQ4MTgxOTIwLCJleHAiOjE3Nzk3MTc5MjB9.zkRPtPQ6Fj2OYWF9LXlDiWPO1_7HGWDGpbfbG4caZBQ"),
                /*TravelImage.Resource(R.drawable.greece1),
                TravelImage.Resource(R.drawable.greece2),
                TravelImage.Resource(R.drawable.beach)*/
            ),
            tags = mutableListOf("Sea", "Party", "Relax"), itinerary = mutableListOf("Boat tour", "Beach day", "Nightlife"),
            activities = mutableListOf("Snorkeling" to true, "Club night" to true, "Island tour" to false), reviews = mutableListOf(),
            questions = mutableListOf("What’s included?"), answers = mutableListOf(Pair(1, "Ferries and accommodation."))
        ),
        "4" to Travel(
            id = "4", title = "Iceland Explorer", owner = "user2" to "bobbyB", description = "Road trip through Iceland's landscapes.",
            dateRange = Pair(timestampFromDateString("01/07/2025")!!, timestampFromDateString("10/07/2025")!!), ageRange = "30 - 45", price = "850", groupSize = "4 - 8",
            participants = linkedMapOf(
                "user3" to Pair(Participant("user3", "charlie", null), true),
                "user7" to Pair(Participant("user7", "graceH", null), true),
                "user2" to Pair(Participant("user2", "bobbyB", null), true)
            ),
            locations = mutableListOf(Triple("01/07/2025", "Reykjavik", true), Triple("05/07/2025", "Vik", true), Triple("08/07/2025", "Akureyri", false)),
            referencedTravel = null, images = mutableListOf(
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/iceland1.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvaWNlbGFuZDEuanBlZyIsImlhdCI6MTc0ODE4MTk2NCwiZXhwIjoxNzc5NzE3OTY0fQ.-jCwMM-paOh7z42L6geJ_u9UWr_1xHn8iM6Gs5CrsgE"),
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/waterfall.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvd2F0ZXJmYWxsLmpwZWciLCJpYXQiOjE3NDgxODE5ODEsImV4cCI6MTc3OTcxNzk4MX0.ChU7Icf8lYQIegT_OivUYc8mrwqZoiLBZK4Siyz1zR0"),
                //TravelImage.RemoteUrl(""),
                /*TravelImage.Resource(R.drawable.iceland1),
                TravelImage.Resource(R.drawable.volcano),
                TravelImage.Resource(R.drawable.waterfall)*/
            ),
            tags = mutableListOf("Nature", "Adventure", "Hiking"), itinerary = mutableListOf("Glacier walk", "Hot springs", "Waterfalls tour"),
            activities = mutableListOf("Hiking" to true, "Photography" to true, "Kayaking" to false), reviews = mutableListOf(),
            questions = mutableListOf("Is equipment provided?"), answers = mutableListOf(Pair(1, "Yes, for hiking."))
        ),
        "5" to Travel(
            id = "5", title = "Tuscany Wine Tour", owner = "user4" to "danaE", description = "Wine and culture in the Tuscan hills.",
            dateRange = Pair(timestampFromDateString("20/05/2025")!!, timestampFromDateString("25/05/2025")!!), ageRange = "30 - 50", price = "500", groupSize = "4 - 10",
            participants = linkedMapOf(
                "user4" to Pair(Participant("user4", "danaE", null), true),
                "user2" to Pair(Participant("user2", "bobbyB", null), true),
                "user3" to Pair(Participant("user3", "charlie", null), true)
            ),
            locations = mutableListOf(Triple("20/05/2025", "Florence", true), Triple("22/05/2025", "Siena", true), Triple("24/05/2025", "Montepulciano", false)),
            referencedTravel = null, images = mutableListOf(
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/tuscany1.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvdHVzY2FueTEuanBlZyIsImlhdCI6MTc0ODE4MjAxMCwiZXhwIjoxNzc5NzE4MDEwfQ.m5RnOLp0K1Ugq1lgMkE43NfmHYjyVZTtKUgeCgRouJw"),
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/vineyard.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvdmluZXlhcmQuanBlZyIsImlhdCI6MTc0ODE4MjAyMSwiZXhwIjoxNzc5NzE4MDIxfQ.0uKEkE73UBe2V5iDmRm5fJjr00ahePDOGGXtT5LM0xI"),
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/wine.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvd2luZS5qcGVnIiwiaWF0IjoxNzQ4MTgyMDMzLCJleHAiOjE3Nzk3MTgwMzN9.IB_B31UKSv9DoOpXHe_Z2RsYaDgDRx6UTV72TB_3Z30"),
                /*TravelImage.Resource(R.drawable.tuscany1),
                TravelImage.Resource(R.drawable.wine),
                TravelImage.Resource(R.drawable.vineyard)*/
            ),
            tags = mutableListOf("Wine", "Culture", "Food"), itinerary = mutableListOf("Wine tasting", "Cooking class", "Vineyard visit"),
            activities = mutableListOf("Tasting" to true, "Sightseeing" to true, "Cycling" to false), reviews = mutableListOf(),
            questions = mutableListOf("Can I join solo?"), answers = mutableListOf(Pair(1, "Yes, solo travelers welcome."))
        ),
        "6" to Travel(
            id = "6", title = "Surf & Yoga Portugal", owner = "user5" to "eveF", description = "Relax and ride the waves in Portugal.",
            dateRange = Pair(timestampFromDateString("01/09/2025")!!, timestampFromDateString("07/09/2025")!!), ageRange = "18 - 35", price = "450", groupSize = "5 - 10",
            participants = linkedMapOf(
                "user5" to Pair(Participant("user5", "eveF", null), true),
                "user6" to Pair(Participant("user6", "frankG", null), true),
                "user7" to Pair(Participant("user7", "graceH", null), true)
            ),
            locations = mutableListOf(Triple("01/09/2025", "Lisbon", true), Triple("03/09/2025", "Ericeira", true), Triple("05/09/2025", "Peniche", false)),
            referencedTravel = null, images = mutableListOf(
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/portugal1.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvcG9ydHVnYWwxLmpwZWciLCJpYXQiOjE3NDgxODIwNjMsImV4cCI6MTc3OTcxODA2M30.w-WVUywcE0AC12hf3KsKdi_gRdf69VbNBP7MyJViZho"),
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/surf.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvc3VyZi5qcGVnIiwiaWF0IjoxNzQ4MTgyMDc1LCJleHAiOjE3Nzk3MTgwNzV9.kgF4fa9WnYl-dCYUeL1pcsVUbvCsOcJPy12fsYWepZY"),
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/yoga.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMveW9nYS5qcGVnIiwiaWF0IjoxNzQ4MTgyMDg4LCJleHAiOjE3Nzk3MTgwODh9.gPRBNQfxswJ3TPqWZWmVW0W3Hrjtd2V7aNQOy7InetI"),
                /*TravelImage.Resource(R.drawable.portugal1),
                TravelImage.Resource(R.drawable.surf),
                TravelImage.Resource(R.drawable.yoga)*/
            ),
            tags = mutableListOf("Surf", "Yoga", "Wellness"), itinerary = mutableListOf("Morning yoga", "Surf lessons", "Evening chill"),
            activities = mutableListOf("Surfing" to true, "Yoga" to true, "Hiking" to false), reviews = mutableListOf(),
            questions = mutableListOf("Do I need experience?"), answers = mutableListOf(Pair(1, "No, all levels welcome."))
        ),
        "7" to Travel(
            id = "7", title = "Andalusian Roadtrip", owner = "user5" to "eveF", description = "Discover the charm of southern Spain.",
            dateRange = Pair(timestampFromDateString("15/06/2025")!!, timestampFromDateString("22/06/2025")!!), ageRange = "25 - 40", price = "550", groupSize = "5 - 10",
            participants = linkedMapOf(
                "user3" to Pair(Participant("user3", "charlie", null), true),
                "user5" to Pair(Participant("user5", "eveF", null), true),
                "user7" to Pair(Participant("user7", "graceH", null), true)
            ),
            locations = mutableListOf(Triple("15/06/2025", "Seville", true), Triple("18/06/2025", "Granada", true), Triple("21/06/2025", "Cordoba", false)),
            referencedTravel = null, images = mutableListOf(
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/alhambra.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvYWxoYW1icmEuanBlZyIsImlhdCI6MTc0ODE4MjEwOCwiZXhwIjoxNzc5NzE4MTA4fQ.CMdlw_PSpE_zdIB_uEg_2et60bTdkRHUTfoRUbLJpLQ"),
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/andalusia1.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvYW5kYWx1c2lhMS5qcGVnIiwiaWF0IjoxNzQ4MTgyMTY2LCJleHAiOjE3Nzk3MTgxNjZ9.jXQ0VWLR20xbRLqfuTh05-JTpzgaY58HwaUBkSl2cTs"),
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/hiking.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvaGlraW5nLmpwZWciLCJpYXQiOjE3NDgxODIxNzYsImV4cCI6MTc3OTcxODE3Nn0.t2gw2ySj0krYo69i27mZbTzl5LwZtAWd-G_OCwA86Cg"),
                /*TravelImage.Resource(R.drawable.andalusia1),
                TravelImage.Resource(R.drawable.alhambra),
                TravelImage.Resource(R.drawable.mezquita)*/
            ),
            tags = mutableListOf("Culture", "Roadtrip", "History", "Flamenco"), itinerary = mutableListOf("Flamenco night", "Alhambra visit", "Tapas tour"),
            activities = mutableListOf("Walking tours" to true, "Museums" to true, "Biking" to false), reviews = mutableListOf(),
            questions = mutableListOf("Is transport included?"), answers = mutableListOf(Pair(1, "Yes, van rental included."))
        ),
        "8" to Travel(
            id = "8", title = "Alps Hiking Week", owner = "2NBchwEdVBXf4C2AYHkFUMvdyk82" to "John01", description = "One week of fresh air and epic views.",
            dateRange = Pair(timestampFromDateString("05/08/2025")!!, timestampFromDateString("12/08/2025")!!), ageRange = "20 - 35", price = "650", groupSize = "4 - 8",
            participants = linkedMapOf(
                "user7" to Pair(Participant("user7", "graceH", null), true),
                "user1" to Pair(Participant("user1", "alice123", null), true),
                "user3" to Pair(Participant("user3", "charlie", null), true)
            ),
            locations = mutableListOf(Triple("05/08/2025", "Chamonix", true), Triple("08/08/2025", "Zermatt", true), Triple("11/08/2025", "St. Moritz", false)),
            referencedTravel = null, images = mutableListOf(
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/alps1.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvYWxwczEuanBlZyIsImlhdCI6MTc0ODE4MjE5NiwiZXhwIjoxNzc5NzE4MTk2fQ.jKbGYyMJ-qGp5xAEO1hZlzr2dA5KiQkYdFfVH0HIAWQ"),
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/mountains.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvbW91bnRhaW5zLmpwZWciLCJpYXQiOjE3NDgxODIyMTAsImV4cCI6MTc3OTcxODIxMH0.xYYCSl7DKCT-fNYOWIiFhnZqX2f-gqRPczELfyBE9R0"),
                //TravelImage.RemoteUrl(""),
                /*TravelImage.Resource(R.drawable.alps1),
                TravelImage.Resource(R.drawable.hiking),
                TravelImage.Resource(R.drawable.mountains)*/
            ),
            tags = mutableListOf("Hiking", "Nature", "Adventure", "Wellness"), itinerary = mutableListOf("Mountain trekking", "Lakeside lunch", "Sunset yoga"),
            activities = mutableListOf("Hiking" to true, "Meditation" to true, "Rock climbing" to false), reviews = mutableListOf(),
            questions = mutableListOf("What should I pack?"), answers = mutableListOf(Pair(1, "Hiking gear, warm clothes."))
        ),
        "9" to Travel(
            id = "9", title = "Moroccan Desert Adventure", owner = "user6" to "frankG", description = "Camel rides, stars, and spice markets.",
            dateRange = Pair(timestampFromDateString("12/10/2025")!!, timestampFromDateString("19/10/2025")!!), ageRange = "18 - 35", price = "700", groupSize = "6 - 12",
            participants = linkedMapOf(
                "user4" to Pair(Participant("user4", "danaE", null), true),
                "user5" to Pair(Participant("user5", "eveF", null), true),
                "user6" to Pair(Participant("user6", "frankG", null), true)
            ),
            locations = mutableListOf(Triple("12/10/2025", "Marrakech", true), Triple("14/10/2025", "Ouarzazate", true), Triple("17/10/2025", "Merzouga", false)),
            referencedTravel = null, images = mutableListOf(
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/morocco1.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvbW9yb2NjbzEuanBlZyIsImlhdCI6MTc0ODE4MjIzNywiZXhwIjoxNzc5NzE4MjM3fQ.7V6fg9Cb6JNklhWdwVRgfhe6lrKYYU4aICDYQ-IMzio"),
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/camel.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvY2FtZWwuanBlZyIsImlhdCI6MTc0ODE4MjI1MiwiZXhwIjoxNzc5NzE4MjUyfQ.sX8U1DOvPbZTdmbZKa55MXc7_2TbNDbGYoutWkm1uI4"),
                TravelImage.RemoteUrl("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/travelimages/desert.jpeg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJ0cmF2ZWxpbWFnZXMvZGVzZXJ0LmpwZWciLCJpYXQiOjE3NDgxODIyNjIsImV4cCI6MTc3OTcxODI2Mn0.jUpBHI8vd9oRbo0ptd1zJoLgEggneWP1usjHIq6DRIA"),
                /*TravelImage.Resource(R.drawable.morocco1),
                TravelImage.Resource(R.drawable.desert),
                TravelImage.Resource(R.drawable.camel)*/
            ),
            tags = mutableListOf("Desert", "Culture", "Adventure", "Food"), itinerary = mutableListOf("Camel trek", "Desert camp", "Spice market tour"),
            activities = mutableListOf("Camel riding" to true, "Stargazing" to true, "Cooking class" to false), reviews = mutableListOf(),
            questions = mutableListOf("Do I need a sleeping bag?"), answers = mutableListOf(Pair(1, "No, camp gear provided."))
        )
    )
    private val StaticUsers: Map<String, UserModel> = mapOf(
        "2NBchwEdVBXf4C2AYHkFUMvdyk82" to UserModel(
            id = "2NBchwEdVBXf4C2AYHkFUMvdyk82",
            firstName = "John",
            lastName = "Doe",
            username = "John01",
            email = "erminio.ottone.mad19@gmail.com",
            bio = "Your bio!",
            hashtag1 = "#Climbing",
            hashtag2 = "#Paragliding",
            hashtag3 = "#Iceland",
            image = UserImage.UrlImage("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/images/boy1.png?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJpbWFnZXMvYm95MS5wbmciLCJpYXQiOjE3NDgxODIyODIsImV4cCI6MTc3OTcxODI4Mn0.N2XM-dKw8gklIMfueypJP0VAH76Wqp9Dl5lPjXJWwe8"),    //.Resource(R.drawable.placeholder_jhon),
            initialReviews = listOf(

                    Reviews(
                    content = "Great travel companion!",
            stars = 5,
            date = Timestamp.now(),
            reviewerUsername = mockRef,
            reviewerImage = mockRef
        )
    )
        ),
        "user1" to UserModel(
            id = "user1",
            firstName = "Alice",
            lastName = "Smith",
            username = "alice123",
            email = "alice.smith@example.com",
            bio = "Enjoys hiking and reading.",
            hashtag1 = "#Hiking",
            hashtag2 = "#Books",
            hashtag3 = "#Travel",
            image = UserImage.UrlImage("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/images/female_img.png?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJpbWFnZXMvZmVtYWxlX2ltZy5wbmciLCJpYXQiOjE3NDgxODIyOTgsImV4cCI6MTc3OTcxODI5OH0.W-0tRZnuiRzgSFyIXWozwuXaolYfE-Qt3QTsEMVKN0A"), //UserImage.Resource(R.drawable.maomao)
        ),
        "user2" to UserModel(
            id = "user2",
            firstName = "Bob",
            lastName = "Brown",
            username = "bobbyB",
            email = "bob.brown@example.com",
            bio = "Guitarist and musician.",
            hashtag1 = "#Music",
            hashtag2 = "#Guitar",
            hashtag3 = "#Live",
            image =  UserImage.UrlImage("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/images/boy2.png?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJpbWFnZXMvYm95Mi5wbmciLCJpYXQiOjE3NDgxODIzMTAsImV4cCI6MTc3OTcxODMxMH0.-sXS8v_dJbD8oVvW9ffjQwTXFry8SsauEZ2x5VdF-hI"), //UserImage.Resource(R.drawable.boy1)
        ),
        "user3" to UserModel(
            id = "user3",
            firstName = "Charlie",
            lastName = "Davis",
            username = "charlie",
            email = "charlie.davis@example.com",
            bio = "Tech enthusiast and coder.",
            hashtag1 = "#Coding",
            hashtag2 = "#Tech",
            hashtag3 = "#Innovation",
            image = UserImage.UrlImage("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/images/boy3.png?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJpbWFnZXMvYm95My5wbmciLCJpYXQiOjE3NDgxODIzMzIsImV4cCI6MTc3OTcxODMzMn0.4-nz8ps3OGDtdzM-snrFtZgMkDfeTyb8TnWSrvzQ-vk"),  //UserImage.Resource(R.drawable.girl2)
        ),
        "user4" to UserModel(
            id = "user4",
            firstName = "Dana",
            lastName = "Evans",
            username = "danaE",
            email = "dana.evans@example.com",
            bio = "Food lover and blogger.",
            hashtag1 = "#Food",
            hashtag2 = "#Blogger",
            hashtag3 = "#Recipes",
            image = UserImage.UrlImage("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/images/maomao.png?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJpbWFnZXMvbWFvbWFvLnBuZyIsImlhdCI6MTc0ODE4MjM0NCwiZXhwIjoxNzc5NzE4MzQ0fQ.XuvEBEXt8tB2fv-157d8UVZE_P8hi2VTjZ6kMIQs5s4"),  //UserImage.Resource(R.drawable.female_img)
        ),
        "user5" to UserModel(
            id = "user5",
            firstName = "Eve",
            lastName = "Foster",
            username = "eveF",
            email = "eve.foster@example.com",
            bio = "Outdoor adventurer.",
            hashtag1 = "#Adventure",
            hashtag2 = "#Nature",
            hashtag3 = "#Photography",
            image = UserImage.UrlImage("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/images/nana.png?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJpbWFnZXMvbmFuYS5wbmciLCJpYXQiOjE3NDgxODIzNjEsImV4cCI6MTc3OTcxODM2MX0.04D1YX2dbEZfV55RjkCaOEojhphjPy_t3QslYEMGSvM"),  //UserImage.Resource(R.drawable.nana)
        ),
        "user6" to UserModel(
            id = "user6",
            firstName = "Frank",
            lastName = "Graham",
            username = "frankG",
            email = "frank.graham@example.com",
            bio = "Fitness fanatic.",
            hashtag1 = "#Fitness",
            hashtag2 = "#Gym",
            hashtag3 = "#Health",
            image = UserImage.UrlImage("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/images/placeholder_jhon.jpg?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJpbWFnZXMvcGxhY2Vob2xkZXJfamhvbi5qcGciLCJpYXQiOjE3NDgxODIzNzIsImV4cCI6MTc3OTcxODM3Mn0.O_2fjN2Hx3lSZdRIr7KU35GKC7u8JubLbiL1lrP46gA"),  //UserImage.Resource(R.drawable.boy2)
        ),
        "user7" to UserModel(
            id = "user7",
            firstName = "Grace",
            lastName = "Hughes",
            username = "graceH",
            email = "grace.hughes@example.com",
            bio = "Traveller and explorer.",
            hashtag1 = "#Travel",
            hashtag2 = "#Adventure",
            hashtag3 = "#Wanderlust",
            image = UserImage.UrlImage("https://rczkrwliuowepjducaoy.supabase.co/storage/v1/object/sign/images/girl2.png?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y1MzkzYWFhLWI2NTYtNGRkZi05YzVjLWMwYzhlZWM1YjJiNSJ9.eyJ1cmwiOiJpbWFnZXMvZ2lybDIucG5nIiwiaWF0IjoxNzQ4MTgyNDAxLCJleHAiOjE3Nzk3MTg0MDF9.dZbSGHKzADKNm-sx1_Z7r9FnBdVhl0nl8m7QUPaA6pA"),  //UserImage.Resource(R.drawable.placeholder_jhon)
        )
    )

    private val StaticParticipants = listOf(
        mapOf("userId" to "user2", "username" to "bobbyB", "enabled" to true, "additionalParticipants" to listOf(mapOf("firstName" to "Joe", "lastName" to "Smith", "birthDate" to "06/01/2000", "phoneNumber" to "+391234567898")), "travelId" to "101"),
        mapOf("userId" to "user5", "username" to "eveF", "null" to true, "additionalParticipants" to null, "travelId" to "101"),
        mapOf("userId" to "user3", "username" to "charlie", "enabled" to true, "additionalParticipants" to null, "travelId" to "101"),

        mapOf("userId" to "user2", "username" to "bobbyB", "enabled" to true, "additionalParticipants" to null, "travelId" to "10"),
        mapOf("userId" to "user5", "username" to "eveF", "enabled" to true, "additionalParticipants" to null, "travelId" to "10"),
        mapOf("userId" to "user3", "username" to "charlie", "enabled" to true, "additionalParticipants" to null, "travelId" to "10"),

        mapOf("userId" to "user5", "username" to "eveF", "enabled" to true, "additionalParticipants" to null, "travelId" to "3"),
        mapOf("userId" to "1", "username" to "John01", "enabled" to true, "additionalParticipants" to null, "travelId" to "3"),
        mapOf("userId" to "user6", "username" to "frankG", "enabled" to true, "additionalParticipants" to null, "travelId" to "3"),

        mapOf("userId" to "user3", "username" to "charlie", "enabled" to true, "additionalParticipants" to null, "travelId" to "4"),
        mapOf("userId" to "user7", "username" to "graceH", "enabled" to true, "additionalParticipants" to null, "travelId" to "4"),
        mapOf("userId" to "user2", "username" to "bobbyB", "enabled" to true, "additionalParticipants" to null, "travelId" to "4"),

        mapOf("userId" to "user4", "username" to "danaE", "enabled" to true, "additionalParticipants" to null, "travelId" to "5"),
        mapOf("userId" to "user2", "username" to "bobbyB", "enabled" to true, "additionalParticipants" to null, "travelId" to "5"),
        mapOf("userId" to "user3", "username" to "charlie", "enabled" to true, "additionalParticipants" to null, "travelId" to "5"),

        mapOf("userId" to "user5", "username" to "eveF", "enabled" to true, "additionalParticipants" to null, "travelId" to "6"),
        mapOf("userId" to "user6", "username" to "frankG", "enabled" to true, "additionalParticipants" to null, "travelId" to "6"),
        mapOf("userId" to "user7", "username" to "graceH", "enabled" to true, "additionalParticipants" to null, "travelId" to "6"),

        mapOf("userId" to "user3", "username" to "charlie", "enabled" to true, "additionalParticipants" to null, "travelId" to "7"),
        mapOf("userId" to "user5", "username" to "eveF", "enabled" to true, "additionalParticipants" to null, "travelId" to "7"),
        mapOf("userId" to "user7", "username" to "graceH", "enabled" to true, "additionalParticipants" to null, "travelId" to "7"),

        mapOf("userId" to "user7", "username" to "graceH", "enabled" to true, "additionalParticipants" to null, "travelId" to "8"),
        mapOf("userId" to "user1", "username" to "alice123", "enabled" to true, "additionalParticipants" to null, "travelId" to "8"),
        mapOf("userId" to "user3", "username" to "charlie", "enabled" to true, "additionalParticipants" to null, "travelId" to "8"),

        mapOf("userId" to "user4", "username" to "danaE", "enabled" to true, "additionalParticipants" to null, "travelId" to "9"),
        mapOf("userId" to "user5", "username" to "eveF", "enabled" to true, "additionalParticipants" to null, "travelId" to "9"),
        mapOf("userId" to "user6", "username" to "frankG", "enabled" to true, "additionalParticipants" to null, "travelId" to "9")
    )


    fun seedAllUsers() {
        val usersCollection = FirebaseFirestore.getInstance().collection("users")

        for ((id, user) in StaticUsers) {
            val imageUrl = when (val img = user.image) {
                is UserImage.UrlImage -> img.url
                is UserImage.UriImage -> img.uri.toString()
                is UserImage.Resource -> "res://${img.resId}"
                else -> null
            }

            val userData = mapOf(
                "id" to user.id,
                "firstName" to user.firstName.value,
                "lastName" to user.lastName.value,
                "username" to user.username.value,
                "email" to user.email.value,
                "bio" to user.bio.value,
                "hashtag1" to user.hashtag1.value,
                "hashtag2" to user.hashtag2.value,
                "hashtag3" to user.hashtag3.value,
                "image" to imageUrl
            )

            usersCollection
                .document(id)
                .set(userData)
                .addOnSuccessListener {
                    Log.d("DBSeeder", "Utente $id salvato")

                    // Subcollection: /users/{id}/reviews
                    val reviewsCollection = usersCollection.document(id).collection("reviews")

                    for (review in user.reviews.value) {
                        val reviewData = mapOf(
                            "reviewerUsername" to review.reviewerUsername,
                            "content" to review.content,
                            "stars" to review.stars,
                            "date" to review.date
                            // NB: non salviamo l’immagine per ora
                        )

                        reviewsCollection
                            .add(reviewData)
                            .addOnSuccessListener {
                                Log.d("DBSeeder", "Review per utente $id salvata")
                            }
                            .addOnFailureListener { e ->
                                Log.e("DBSeeder", "Errore salvataggio review per utente $id", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DBSeeder", "Errore salvataggio utente $id", e)
                }
        }
    }

    fun seedAllParticipants(){
        val pCollection = FirebaseFirestore.getInstance().collection("participants")

        for (p in StaticParticipants){
            val pData = mapOf(
                "userId" to Firebase.firestore.document("users/${p["userId"]}"),
                "username" to p["username"],
                "enabled" to p["enabled"],
                "additionalParticipants" to p["additionalParticipants"],
                "travelId" to Firebase.firestore.document("travels/${p["travelId"]}")
            )
            val id = "${p["userId"]}" + "," + "${p["travelId"]}"

            pCollection.document(id).set(pData)
        }
    }

    fun seedAllTravels() {
        for ((id, travel) in StaticTravels) {
            val travelData = hashMapOf(
                "id" to travel.id,
                "title" to travel.title,
                "description" to travel.description,
                "owner" to Firebase.firestore.document("users/${travel.owner.first}"),
                "ownerName" to travel.owner.second,
                "startDate" to travel.dateRange.first,
                "endDate" to travel.dateRange.second,
                "ageRange" to travel.ageRange,
                "price" to travel.price.toDouble(),
                "groupSize" to travel.groupSize,
                "tags" to travel.tags,
                "itinerary" to travel.itinerary,
                "questions" to travel.questions,
                "answers" to travel.answers.map { mapOf("questionIndex" to it.first, "answer" to it.second) },
                "activities" to travel.activities.map { mapOf("name" to it.first, "enabled" to it.second) },
                "locations" to travel.locations.map { mapOf("date" to it.first, "place" to it.second, "overnight" to it.third) },
                "places" to travel.locations.map { it.second.trim().lowercase() },
                "referencedTravel" to travel.referencedTravel,
                "images" to travel.images.map {
                    when (val img = it) {
                        is TravelImage.RemoteUrl -> img.url
                        is TravelImage.UriImage -> img.uri.toString()
                        is TravelImage.Resource -> "res://${img.resId}"
                        else -> null
                    } }
            )

            db.collection("travels")
                .document(travel.id)
                .set(travelData)
                .addOnSuccessListener {
                    Log.d("DBSeeder", "Travel ${travel.id} salvato")
                    seedParticipantsForTravel(travel)
                     }
                .addOnFailureListener { e ->
                    Log.e("DBSeeder", "Errore salvataggio travel ${travel.id}", e)
                }
        }
    }


    private fun seedParticipantsForTravel(travel: Travel) {
        val participantsCollection = db.collection("travels").document(travel.id).collection("participants")

        for ((_, pair) in travel.participants) {
            val participant = pair.first
            val enabled = pair.second

            val participantData = hashMapOf(
                "userRef" to Firebase.firestore.document("users/${participant.userId}"),
                "enabled" to enabled,
                "additionalParticipants" to participant.additionalParticipants?.map {
                    mapOf(
                        "name" to it.name,
                        "surname" to it.surname,
                        "birthDate" to it.birthDate,
                        "cellphone" to it.cellphone
                    )
                }
            )

            participantsCollection
                .document(participant.userId)
                .set(participantData)
                .addOnSuccessListener {
                    Log.d("DBSeeder", "Partecipant ${participant.userId} salvato in travel ${travel.id}")
                }
                .addOnFailureListener {
                    Log.e("DBSeeder", "Errore nel salvataggio partecipant ${participant.userId} in ${travel.id}", it)
                }
        }
    }

    fun clearFirestoreDatabase(onComplete: () -> Unit = {}) {
        val db = FirebaseFirestore.getInstance()

        fun deleteSubcollections(docRef: DocumentReference, onFinish: () -> Unit) {
            docRef.collection("reviews").get().addOnSuccessListener { revs ->
                val revsJobs = revs.documents.map { it.reference.delete() }

                docRef.collection("participants").get().addOnSuccessListener { parts ->
                    val partsJobs = parts.documents.map { it.reference.delete() }

                    // Attendi tutte le delete (semplificato)
                    Tasks.whenAllComplete(revsJobs + partsJobs).addOnSuccessListener {
                        onFinish()
                    }
                }
            }
        }

        fun deleteCollection(collectionPath: String, onFinish: () -> Unit) {
            db.collection(collectionPath)
                .get()
                .addOnSuccessListener { snapshot ->
                    val docs = snapshot.documents
                    if (docs.isEmpty()) {
                        onFinish()
                        return@addOnSuccessListener
                    }

                    var deleted = 0
                    for (doc in docs) {
                        // Elimina subcollections (es. reviews, participants) se presenti
                        deleteSubcollections(doc.reference) {
                            doc.reference.delete()
                                .addOnSuccessListener {
                                    deleted++
                                    if (deleted == docs.size) onFinish()
                                }
                                .addOnFailureListener {
                                    Log.e("DBClear", "Errore eliminando ${doc.reference.path}", it)
                                }
                        }
                    }
                }
        }

        // In parallelo: svuota users e travels
        deleteCollection("users") {
            Log.d("DBClear", "✔️ Tutti gli utenti eliminati")
            deleteCollection("travels") {
                Log.d("DBClear", "✔️ Tutti i viaggi eliminati")
                deleteCollection("participants") {
                    onComplete()
                }
            }
        }
    }

    fun timestampFromDateString(dateString: String): Timestamp? {
        return try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsedDate: Date = formatter.parse(dateString)!!

            // Imposta un orario fisso (es. mezzogiorno) per evitare problemi di fuso
            val calendar = java.util.Calendar.getInstance().apply {
                time = parsedDate
                set(java.util.Calendar.HOUR_OF_DAY, 10)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }

            Timestamp(calendar.time)
        } catch (e: Exception) {
            null // In caso di formato errato
        }
    }
}