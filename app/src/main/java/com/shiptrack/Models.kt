package com.shiptrack

data class Zone(
    val code: String,
    val name: String,
    val icon: String,
    val desc: String
)

data class Category(val name: String)

object DefaultData {
    val ZONES = listOf(
        Zone("ZONE-A1", "Bow Section",       "", "Forward hull and anchor"),
        Zone("ZONE-A2", "Forward Deck",      "", "Deck equipment"),
        Zone("ZONE-B1", "Midship Port",      "", "Port side"),
        Zone("ZONE-B2", "Midship Stbd",      "", "Starboard side"),
        Zone("ZONE-C1", "Engine Room",       "", "Main propulsion"),
        Zone("ZONE-C2", "Aft Section",       "", "Stern and rudder"),
        Zone("ZONE-D1", "Drydock 1",         "", "Drydock bay 1"),
        Zone("ZONE-D2", "Drydock 2",         "", "Drydock bay 2"),
        Zone("ZONE-E1", "Fabrication Shop",  "", "Metal fab"),
        Zone("ZONE-E2", "Pipe Workshop",     "", "Pipe fitting")
    )
    val CATEGORIES = listOf("Pipeline","Equipment","Foundation","Structural","Electrical","Other")
    val SEED_TASKS = listOf(
        Task("TASK 001","Inspect P-203 flange gasket","Pipeline","ZONE-C1",listOf("ZONE-C1"),"Critical","Open","2026-03-18","P-203","LOTO required",emptyList(),Millis(-2)),
        Task("TASK 002","Replace HVAC brackets","Structural","ZONE-B1",listOf("ZONE-B1"),"High","In Progress","2026-03-20","HVAC-B1","3mm 316L plate",emptyList(),Millis(-5)),
        Task("TASK 003","Align main engine coupling","Equipment","ZONE-C1",listOf("ZONE-C1"),"High","Open","2026-03-22","ME-PORT-01","Laser alignment",emptyList(),Millis(-3)),
        Task("TASK 004","Pour foundation pad","Foundation","ZONE-D1",listOf("ZONE-D1"),"Medium","In Progress","2026-03-25","KEEL-B1","QA sign-off needed",emptyList(),Millis(-7)),
        Task("TASK 005","Commission power panel","Electrical","ZONE-A2",listOf("ZONE-A2"),"Medium","Open","2026-03-28","SP-04","Coordinate with electrical team",emptyList(),Millis(-1)),
        Task("TASK 006","Weld repair keel plate","Structural","ZONE-D2",listOf("ZONE-D2"),"High","Hold On","2026-03-19","KEEL-K-07","Class hold",emptyList(),Millis(-4))
    )
    private fun Millis(days: Long) = System.currentTimeMillis() + days * 86400000L
    val TYPE_ICONS = mapOf("Pipeline" to "","Equipment" to "","Foundation" to "","Structural" to "","Electrical" to "","Other" to "")
}
