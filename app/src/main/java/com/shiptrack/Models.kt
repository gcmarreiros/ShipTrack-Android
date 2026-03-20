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
        Zone("ZONE-A1", "Bow Section",       "⚓", "Forward hull & anchor systems"),
        Zone("ZONE-A2", "Forward Deck",      "🏗", "Deck equipment & fittings"),
        Zone("ZONE-B1", "Midship Port",      "⬡", "Port side machinery & pipes"),
        Zone("ZONE-B2", "Midship Stbd",      "⬡", "Starboard side systems"),
        Zone("ZONE-C1", "Engine Room",       "⚙", "Main propulsion & auxiliaries"),
        Zone("ZONE-C2", "Aft Section",       "🔩", "Stern, rudder & shaft"),
        Zone("ZONE-D1", "Drydock #1",        "🏭", "Drydock construction bay 1"),
        Zone("ZONE-D2", "Drydock #2",        "🏭", "Drydock construction bay 2"),
        Zone("ZONE-E1", "Fabrication Shop",  "🔧", "Metal fab & assembly"),
        Zone("ZONE-E2", "Pipe Workshop",     "⌁",  "Pipe fitting & spooling")
    )
    val CATEGORIES = listOf("Pipeline","Equipment","Foundation","Structural","Electrical","Other")
    val SEED_TASKS = listOf(
        Task("TASK 001","Inspect P-203 flange gasket","Pipeline","ZONE-C1",listOf("ZONE-C1"),"Critical","Open","2026-03-18","P-203 / FW-LINE-08","LOTO required.",emptyList(),System.currentTimeMillis()-86400000L*2),
        Task("TASK 002","Replace corroded HVAC brackets","Structural","ZONE-B1",listOf("ZONE-B1"),"High","In Progress","2026-03-20","HVAC-B1-04","Fabricate replacements.",emptyList(),System.currentTimeMillis()-86400000L*5),
        Task("TASK 003","Align main engine coupling","Equipment","ZONE-C1",listOf("ZONE-C1"),"High","Open","2026-03-22","ME-PORT-01","Vibration >0.05mm.",emptyList(),System.currentTimeMillis()-86400000L*3),
        Task("TASK 004","Pour foundation pad","Foundation","ZONE-D1",listOf("ZONE-D1"),"Medium","In Progress","2026-03-25","KEEL-BLK-14","Wait for QA sign-off.",emptyList(),System.currentTimeMillis()-86400000L*7),
        Task("TASK 005","Commission shore power panel","Electrical","ZONE-A2",listOf("ZONE-A2"),"Medium","Open","2026-03-28","SP-04","Coordinate with team.",emptyList(),System.currentTimeMillis()-86400000L*1),
        Task("TASK 006","Weld repair keel plate","Structural","ZONE-D2",listOf("ZONE-D2"),"High","Hold On","2026-03-19","KEEL-K-07","Contact DNV surveyor.",emptyList(),System.currentTimeMillis()-86400000L*4)
    )
    val TYPE_ICONS = mapOf("Pipeline" to "⌁","Equipment" to "⚙","Foundation" to "⫛","Structural" to "△","Electrical" to "⚡","Other" to "◌")
}
