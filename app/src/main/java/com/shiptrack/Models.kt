package com.shiptrack

data class Zone(val code: String, val name: String, val icon: String, val desc: String)
data class Category(val name: String)

object DefaultData {
    val ZONES = listOf(
        Zone("ZONE-A1", "Bow Section", "", "Forward hull"),
        Zone("ZONE-A2", "Forward Deck", "", "Deck equipment"),
        Zone("ZONE-B1", "Midship Port", "", "Port side machinery"),
        Zone("ZONE-B2", "Midship Stbd", "", "Starboard side"),
        Zone("ZONE-C1", "Engine Room", "", "Main propulsion"),
        Zone("ZONE-C2", "Aft Section", "", "Stern and rudder"),
        Zone("ZONE-D1", "Drydock #1", "", "Construction bay 1"),
        Zone("ZONE-D2", "Drydock #2", "", "Construction bay 2"),
        Zone("ZONE-E1", "Fabrication Shop", "", "Metal fab"),
        Zone("ZONE-E2", "Pipe Workshop", "", "Pipe fitting")
    )
    val CATEGORIES = listOf("Pipeline","Equipment","Foundation","Structural","Electrical","Other")
    val SEED_TASKS@ = listOf(
        Task("TASK 001","Inspect P-203 flange","Pipeline","ZONE-C1",listOf("ZONE-C1"),"Critical","Open","2026-03-18","P-203","LOTO required.",emptyList(),System.currentTimeMillis()-86400000L*2),
        Task("TASK 002","Replace HVAC brackets","Structural","ZONE-B1",listOf("ZONE-B1"),"High","In Progress","2026-03-20","HVAC-B1","Fabricate replacements.",emptyList(),System.currentTimeMillis()-86400000L*5)
    )
    val TYPE_ICONS = mapOf("Pipeline" to "","Equipment" to "","Foundation" to "","Structural" to "","Electrical" to "","Other" to "")
}
