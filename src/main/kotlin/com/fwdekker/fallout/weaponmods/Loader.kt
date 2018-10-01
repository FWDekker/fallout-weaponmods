package com.fwdekker.fallout.weaponmods

import com.beust.klaxon.Klaxon
import java.io.File
import kotlin.system.exitProcess


data class GameDatabase(
    val looseMods: List<LooseMod>,
    val objectModifiers: List<ObjectModifier>,
    val craftableObjects: List<CraftableObject>,
    val components: List<Component>
) {
    constructor(directory: File) : this(
        Klaxon().parseArray<LooseMod>(File(directory, "misc.json").inputStream())!!,
        Klaxon().parseArray<ObjectModifier>(File(directory, "omod.json").inputStream())!!,
        Klaxon().parseArray<CraftableObject>(File(directory, "cobj.json").inputStream())!!,
        Klaxon().parseArray<Component>(File(directory, "cmpo.json").inputStream())!!
    )
}

data class WeaponMod(
    val esm: ESM?,
    val formIDTemplate: String,
    val weaponLink: Pair<String, String?>,
    val effects: String,
    val value: Int,
    val weight: Double,
    val image: String
) {
    constructor(looseMod: LooseMod, objectModifier: ObjectModifier, craftableObject: CraftableObject) :
        this(
            esm = ESM.valueOf(looseMod.file.takeWhile { it != '.' }),
            formIDTemplate = looseMod.formID.toString(16).let {
                if (it.length > 6) "{{DLC ID|${it.take(6)}}}"
                else "{{ID|$it}}"
            },
            weaponLink = keywordToWeapon(objectModifier.weaponName),
            effects = objectModifier.description,
            value = looseMod.value,
            weight = looseMod.weight,
            image = modelToImage(looseMod.model)
        ) {
        require(looseMod.file == objectModifier.file && objectModifier.file == craftableObject.file) { "?" }
        // TODO improve message
    }

    companion object {
        fun create(looseMod: LooseMod, database: GameDatabase): WeaponMod? {
            val objectModifier = database.objectModifiers.singleOrNull { it.looseMod == looseMod.editorID }
            if (objectModifier == null) {
                println("Could not create weapon mod object") // TODO use logger
                return null
            }

            val craftableObject = database.craftableObjects.singleOrNull { it.createdMod == objectModifier.editorID }
            if (craftableObject == null) {
                println("Could not create weapon mod object")
                return null
            }

            return WeaponMod(looseMod, objectModifier, craftableObject)
        }
    }
}

enum class ESM(
    val title: String,
    val pageName: String?,
    val abbreviation: String,
    val categories: Boolean
) {
    Fallout4("Fallout 4", null, "FO4", true),
    DLCCoast("Far Harbor", "Far Harbor (add-on)", "FO4FH", false),
    DLCNukaWorld("Nuka-World", "Nuka-World (add-on)", "FO4NW", true),
    DLCAutomatron("Automatron", "Automatron (add-on)", "FO4AUT", true),
    DLCWorkshop01("Wasteland Workshop", null, "FO4WW", false),
    DLCWorkshop02("Contraptions Workshop", null, "FO4CW", false),
    DLCWorkshop03("Vault-Tec Workshop", null, "FO4VW", false);

    fun getWikiLink() =
        if (pageName == null) "''[[$title]]''"
        else "''[[$pageName|$title]]''"
}

fun keywordToWeapon(keyword: String): Pair<String, String?> =
    when (keyword.toLowerCase()) {
        "ma_10mm" -> Pair("10mm pistol", "10mm pistol (Fallout 4)")
        "ma_44" -> Pair(".44 pistol", null)
        "ma_assaultrifle" -> Pair("assault rifle", "Assault rifle (Fallout 4)")
        "ma_broadsider" -> Pair("Broadsider", null)
        "ma_combatgun" -> Pair("combat shotgun", "Combat shotgun (Fallout 4)")
        "ma_combatrifle" -> Pair("combat rifle", null)
        "ma_cryolator" -> Pair("Cryolator", null)
        "ma_deliverer" -> Pair("Deliverer", null)
        "ma_doublebarrelshotgun" -> Pair("double-barrel shotgun", "Double-barrel shotgun (Fallout 4)")
        "ma_flamer" -> Pair("flamer", "Flamer (Fallout 4)")
        "ma_gatlinglaser" -> Pair("Gatling laser", "Gatling laser (Fallout 4)")
        "ma_gaussrifle" -> Pair("Gauss rifle", "Gauss rifle (Fallout 4)")
        "ma_institutelasergun" -> Pair("Institute laser gun", null)
        "ma_laser gun" -> Pair("laser gun", null)
        "ma_lasermusket" -> Pair("laser musket", null)
        "ma_huntingrifle" -> Pair("hunting rifle", "Hunting rifle (Fallout 4)")
        "ma_minigun" -> Pair("minigun", "Minigun (Fallout 4)")
        "ma_missilelauncher" -> Pair("missile launcher", "Missile launcher (Fallout 4)")
        "ma_pipeboltaction" -> Pair("pipe bolt-action", null)
        "ma_pipegun" -> Pair("pipe gun", null)
        "ma_piperevolver" -> Pair("pipe revolver", null)
        "ma_plasmagun" -> Pair("plasma gun", null)
        "ma_submachinegun" -> Pair("submachine gun", "Submachine gun (Fallout 4)")
        "ma_supersledge" -> Pair("super sledge", "Super sledge (Fallout 4)")
        "dlc01ma_lightninggun" -> Pair("Tesla rifle", null)
        "dlc03_ma_levergun" -> Pair("lever-action rifle", "Lever-action rifle (Far Harbor)")
        "dlc03_ma_radiumrifle" -> Pair("radium rifle", null)
        "dlc04_ma_handmadeassaultrifle" -> Pair("handmade rifle", null)
        "dlc04_ma_revolver" -> Pair("western revolver", null)
        else -> Pair("???", "???").also { println("Unknown keyword $keyword") }
    }

fun modelToImage(model: String): String =
    when (model.toLowerCase()) {
        "props\\modspartbox\\modbox.nif" -> "Fo4 item Mod type A.png"
        "props\\modspartbox\\modcrate.nif" -> "Fo4 item Mod type B.png"
        else -> "".also { println("Unrecognised mod model") }
    }

fun namedAggregation(weaponMods: List<WeaponMod>, property: (WeaponMod) -> String) =
    if (weaponMods.map(property).distinct().size == 1)
        property(weaponMods[0]) // TODO check if empty
    else
        weaponMods.joinToString("<br />") { "${property(it)} (${it.weaponLink.first})" }

fun main(args: Array<String>) {
    print("Enter JSON location: ")
    val databaseLocation = readLine() ?: exitProcess(-1)
    val database = GameDatabase(File(databaseLocation))

    print("Enter weapon mod name: ")
    val targetName = readLine() ?: exitProcess(-1)

    launch(database, targetName)
}

private fun launch(database: GameDatabase, modName: String) {
    val looseMods = database.looseMods.asSequence().filter { it.name.toLowerCase().contains(modName) }.toList()
    val weaponMods = looseMods.asSequence()
        .mapNotNull { WeaponMod.create(it, database) }
        .sortedBy { it.weaponLink.first }
        .toList()
    if (weaponMods.isEmpty()) {
        println("No weapon mods found")
        exitProcess(-1)
    }

    val image =
        weaponMods.groupingBy { it.image }.eachCount().entries.maxBy { it.value }?.key ?: "" // TODO log empty image
    val games = weaponMods.asSequence().mapNotNull { it.esm }.distinct().toList().sortedBy { it.title }
    val appearanceString =
        if (games.size == 1 && games[0] == ESM.Fallout4)
            games[0].getWikiLink()
        else if (games.size == 2 && games.contains(ESM.Fallout4))
            "${ESM.Fallout4.getWikiLink()} and its [[Fallout 4 add-ons|add-on]] ${games.asSequence()
                .filterNot { it == ESM.Fallout4 }
                .first()
                .getWikiLink()
            }"
        else
            "${ESM.Fallout4.getWikiLink()} and its [[Fallout 4 add-ons|add-ons]] ${games.dropLast(1)
                .asSequence()
                .filterNot { it == ESM.Fallout4 }
                .map { it.getWikiLink() }
                .joinToString(", ")
            } and ${games.last().getWikiLink()}"

    println("""
{{Infobox item
|games        =${games.joinToString(", ") { it.abbreviation }}
|type         =mod
|icon         =
|image        =$image
|effects      =<!-- Variable --> // TODO
|modifies     =${weaponMods.joinToString("<br />") {
        if (it.weaponLink.second == null) "[[${it.weaponLink.first.capitalize()}]]"
        else "[[${it.weaponLink.second}|${it.weaponLink.first.capitalize()}]]" // TODO make this less ugly
    }}
|value        =${namedAggregation(weaponMods) { it.value.toString() }}
|weight       =${namedAggregation(weaponMods) { it.weight.toString() }}
|baseid       =${namedAggregation(weaponMods) { it.formIDTemplate }}
}}{{Games|${games.joinToString("|") { it.abbreviation }}}}

The '''$modName''' is a [[Fallout 4 weapon mods|weapon mod]] in $appearanceString.

==Effects==
<!-- Variable --> // TODO

==Production==
<!-- Variable --> // TODO

==Locations==
The $modName can be crafted at any [[weapons workbench]].

{{Navbox weapon mods FO4}}

${games.asSequence()
        .filter { it.categories }
        .joinToString("\n") { "[[Category:${it.title} weapon mods]]" }}
    """.trimIndent())
}
