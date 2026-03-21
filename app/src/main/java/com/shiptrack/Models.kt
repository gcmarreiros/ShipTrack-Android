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
        Zone("ZONE-A1", "Bow Section",       "\u2693", "Forward hull & anchor systems"),
        Zone("ZONE-A2", "Forward Deck",      "\uD83C\uDFD7", "Deck equipment & fittings"),
        Zone("ZONE-B1", "Midship Port",      "\u2B21", "Port side machinery & pipes"),
        Zone("ZONE-B2", "Midship Stbd",      "\u2B21", "Starboard side systems"),
        Zone("ZONE-C1", "Engine Room",       "\u2699", "Main propulsion & auxiliaries"),
        Zone("ZONE-C2", "Aft Section",       "\uD83D\uDD29", "Stern, rudder & shaft"),
        Zone("ZONE-D1", "Drydock #1",        "\uD83C\uDFED", "Drydock construction bay 1"),
        Zone("ZONE-D2", "Drydock #2",        "\uD83C\uDFED", "Drydock construction bay 2"),
        Zone("ZONE-E1", "Fabrication Shop",  "\uD83D\uDD27", "Metal fab & assembly"),
        Zone("ZONE-E2", "Pipe Workshop",     "\u2301",  "Pipe fitting & spooling")
    )

    val CATEGORIES = listOf("Pipeline","Equipment","Foundation","Structural","Electrical","Other")

    val SEED_TASKS = listOf(
        Task("TASK 001","Inspect P-203 flange gasket \u2013 possible leak","Pipeline","ZONE-C1",
            listOf("ZONE-C1"),"Critical","Open","2026-03-18","P-203 / FW-LINE-08",
            "LOTO required. Notify safety officer before opening.", emptyList(),
            System.currentTimeMillis()-86400000L*2),
        Task("TASK 002","Replace corroded HVAC support brackets","Structural","ZONE-B1",
            listOf("ZONE-B1"),"High","In Progress","2026-03-20","HVAC-B1-04",
            "Fabricate replacements in E1. 3mm 316L plate.", emptyList(),
            System.currentTimeMillis()-86400000L*5),
        Task("TASK 003","Align main engine coupling \u2013 port shaft","Equipment","ZONE-C1",
            listOf("ZONE-C1"),"High","Open","2026-03-22","ME-PORT-01",
            "Vibration >0.05mm. Laser alignment tool from tool store.", emptyList(),
            System.currentTimeMillis()-86400000L*3),
        Task("TASK 004","Pour foundation pad \u2013 crane pedestal Block 14","Foundation","ZONE-D1",
            listOf("ZONE-D1"),"Medium","In Progress","2026-03-25","KEEL-BLK-14",
            "Wait for QA sign-off on rebar before pour.", emptyList(),
            System.currentTimeMillis()-86400000L*7),
        Task("TASK 005","Commission shore power panel SP-04","Electrical","ZONE-A2",
            listOf("ZONE-A2"),"Medium","Open","2026-03-28","SP-04 / HV-SHORE-04",
            "Coordinate with electrical safety team.", emptyList(),
            System.currentTimeMillis()-86400000L*1),
        Task("TASK 006","Weld repair keel plate \u2013 Section K-07","Structural","ZONE-D2",
            listOf("ZONE-D2"),"High","Hold On","2026-03-19","KEEL-K-07",
            "Contact DNV surveyor. Class hold in place.", emptyList(),
            System.currentTimeMillis()-86400000L*4)
    )

    val TYPE_ICONS = mapOf(
        "Pipeline" to "\u2301", "Equipment" to "\u2699", "Foundation" to "\u2B1B",
        "Structural" to "\u25B3", "Electrical" to "\u26A1", "Other" to "\u25CC"
    )
}
