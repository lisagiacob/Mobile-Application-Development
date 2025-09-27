package com.example.lab2

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseManager {

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = "https://rczkrwliuowepjducaoy.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJjemtyd2xpdW93ZXBqZHVjYW95Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc5OTU0ODQsImV4cCI6MjA2MzU3MTQ4NH0.3Dt-alPc72MYZyxUlSZ4fwG3P9CUjej9q71pz7F62ko",
        ) {
            install(Storage)
            install(GoTrue)
            //install(Postgrest)
        }
    }
}