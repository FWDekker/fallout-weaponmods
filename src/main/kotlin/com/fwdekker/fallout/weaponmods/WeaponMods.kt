package com.fwdekker.fallout.weaponmods

import com.beust.klaxon.JsonValue
import com.fwdekker.fallout.weaponmods.wiki.Article
import com.fwdekker.fallout.weaponmods.wiki.Category
import com.fwdekker.fallout.weaponmods.wiki.CraftingTable
import com.fwdekker.fallout.weaponmods.wiki.Section
import com.fwdekker.fallout.weaponmods.wiki.WeaponModEffectTable
import com.fwdekker.fallout.weaponmods.wiki.WikiTemplate
import com.fwdekker.fallout.weaponmods.xedit.CraftableObject
import com.fwdekker.fallout.weaponmods.xedit.GameDatabase
import com.fwdekker.fallout.weaponmods.xedit.LooseMod
import com.fwdekker.fallout.weaponmods.xedit.ObjectModifier
import mu.KLogging
import java.io.File
import kotlin.system.exitProcess
import com.fwdekker.fallout.weaponmods.xedit.Weapon as XEditWeapon


/**
 * A form ID.
 *
 * @property addOn whether the form ID is for an add-on
 * @property id the six-digit lowercase hexadecimal form ID
 */
data class FormID(val addOn: Boolean, val id: String) : WikiTemplate(
    if (addOn) "DLC ID" else "ID",
    listOf("1" to id)
) {
    init {
        require(Regex("[0-9a-fA-F]*").matches(id)) { "Form IDs must be hexadecimal." }
        require(id.length == 6) { "Form IDs must have six hexadecimal numbers." }
        require(id == id.toLowerCase()) { "Form IDs must be in lowercase." }
    }


    class Converter : com.beust.klaxon.Converter {
        override fun canConvert(cls: Class<*>) = cls == FormID::class.java

        override fun fromJson(jv: JsonValue) = FormID.fromString(jv.string!!)

        override fun toJson(value: Any) = "\"${(value as FormID).id}\""


        @Target(AnnotationTarget.FIELD)
        annotation class Annotation
    }


    companion object {
        /**
         * Transforms a string into a form ID.
         *
         * @param id a string
         */
        fun fromString(id: String): FormID {
            val addOn = id.dropWhile { it == '0' }
            return FormID(addOn.length > 6, addOn.takeLast(6).toLowerCase().padStart(6, '0'))
        }
    }
}

data class WeaponMod(
    val item: LooseMod,
    val effects: ObjectModifier,
    val recipe: CraftableObject
) {
    companion object : KLogging() {
        fun fromLooseMod(looseMod: LooseMod, database: GameDatabase): WeaponMod? {
            val objectModifier = database.objectModifiers.singleOrNull { it.looseMod == looseMod }
            if (objectModifier == null) {
                logger.warn { "Could not create weapon mod with omod `${looseMod.editorID}`." }
                return null
            }
            if (objectModifier.weapon?.wikiLink == null) {
                logger.warn { "Could not create weapon mod with cobj `${objectModifier.editorID}`." }
                return null
            }

            val craftableObject = database.craftableObjects.singleOrNull { it.createdMod == objectModifier }
            if (craftableObject == null) {
                logger.warn { "Could not create weapon mod with cobj `${objectModifier.editorID}`." }
                return null
            }

            return fromObjects(looseMod, objectModifier, craftableObject)
        }

        private fun fromObjects(
            looseMod: LooseMod,
            objectModifier: ObjectModifier,
            craftableObject: CraftableObject
        ): WeaponMod {
            require(looseMod.file == objectModifier.file && objectModifier.file == craftableObject.file) { "?" }

            return WeaponMod(
                looseMod,
                objectModifier,
                craftableObject
            )
        }
    }
}

class WeaponSelection(
    private val gameDatabase: GameDatabase, // TODO remove database parameter
    private val modName: String,
    private val weaponMods: List<WeaponMod>
) {
    private val image = weaponMods
        .sortedBy { it.effects.weapon!!.wikiLink!!.text }
        .filterNot { it.item.model == null }
        .groupingBy { it.item.model!!.image } // TODO remove !!
        .eachCount().entries
        .maxBy { it.value }?.key
        ?: "" // TODO log empty image
    private val games = weaponMods
        .map { it.item.file }
        .distinct()
        .toList()
        .sortedBy { it.name }

    private val appearanceString = // TODO clean up
        if (games.size == 1 && games[0] == gameDatabase.files.single { it.fileName == "Fallout4.esm" })
            "''${games[0].link}''"
        else if (games.size == 2 && games.contains(gameDatabase.files.single { it.fileName == "Fallout4.esm" }))
            "''${gameDatabase.files.single { it.fileName == "Fallout4.esm" }.link
            }'' and its [[Fallout 4 add-ons|add-on]] ''${games.asSequence()
                .filterNot { it == gameDatabase.files.single { file -> file.fileName == "Fallout4.esm" } }
                .first()
                .link
            }''"
        else
            "''${gameDatabase.files.single { it.fileName == "Fallout4.esm" }.link
            }'' and its [[Fallout 4 add-ons|add-ons]] ${games.dropLast(
                1)
                .asSequence()
                .filterNot { it == gameDatabase.files.single { file -> file.fileName == "Fallout4.esm" } }
                .map { it.link }
                .joinToString(", ") { "''$it''" }
            } and ''${games.last().link}''"


    private fun createInfobox(): WikiTemplate {
        return WikiTemplate(
            "Infobox item",
            listOf(
                "games" to games.joinToString(", ") { it.abbreviation },
                "type" to "mod",
                "icon" to "",
                "image" to image,
                "effects" to "<!-- Variable -->", // TODO
                "modifies" to weaponMods
                    .sortedBy { it.effects.weapon!!.wikiLink!!.text }
                    .joinToString("<br />") { it.effects.weapon!!.wikiLink!!.toString(capitalize = true) },
                "value" to namedAggregation { it.item.value.toString() },
                "weight" to namedAggregation { it.item.weight.toString() },
                "baseid" to namedAggregation { it.item.formID.toString(multiline = false) }
            )
        )
    }

    private fun createEffects(): Section {
        return Section(
            "Effects",
            WeaponModEffectTable(weaponMods.sortedBy { it.effects.weapon!!.wikiLink!!.text }).toString()
        )
    }

    private fun createProductionTable(mod: WeaponMod): String {
        return CraftingTable(
            materials = mod.recipe.components.map { it.key.name to it.value },
            workspace = "[[Weapons workbench]]",
            perks = mod.recipe.conditions.map { Pair(it.key.name, it.value) }, // TODO insert link to perk
            products = listOf(modName.capitalize() to 1)
        ).toString()
    }

    private fun createProduction(): Section {
        return if (weaponMods.size == 1)
            Section("Production", createProductionTable(weaponMods[0]))
        else
            Section("Production",
                "",
                subsections = weaponMods
                    .sortedBy { it.effects.weapon!!.wikiLink!!.text }
                    .map { weaponMod ->
                        Section(
                            weaponMod.effects.weapon!!.wikiLink!!.toString(capitalize = true),
                            createProductionTable(weaponMod),
                            level = 3
                        )
                    })
    }

    private fun createLocation(): Section {
        return Section("Location",
            "The $modName can be crafted at any [[weapons workbench]].")
    }


    fun createPage(): Article {
        return Article().also { page ->
            page.games += games.map { it.abbreviation }
            page.infoboxes += createInfobox()
            page.intro = "The '''$modName''' is a [[Fallout 4 weapon mods|weapon mod]] in $appearanceString."
            page.sections +=
                listOf(
                    createEffects(),
                    createProduction(),
                    createLocation()
                )
            page.navboxes += WikiTemplate("Navbox weapon mods FO4")
            page.categories += games.mapNotNull { it.modCategory }.map {
                Category(it)
            }
        }
    }


    private fun namedAggregation(property: (WeaponMod) -> String): String {
        // TODO remove this function?
        return if (weaponMods.map(property).distinct().size == 1)
            property(weaponMods[0]) // TODO check if empty
        else
            weaponMods
                .sortedBy { it.effects.weapon!!.wikiLink!!.text }
                .joinToString("<br />") { "${property(it)} (${it.effects.weapon!!.wikiLink!!.text})" }
    }
}


fun main(args: Array<String>) {
    val logger = KLogging().logger

    print("Enter JSON location: ")
    val databaseLocation = readLine()
        ?: logger.error { "No location was entered." }.let { exitProcess(-1) }
    val database = GameDatabase.fromDirectory(File(databaseLocation))
        ?: logger.error { "Failed to read database." }.let { exitProcess(-1) }

    while (true) {
        print("Enter weapon mod name: ")
        val targetName = readLine()
            ?: logger.error { "No weapon mod was entered." }.let { exitProcess(-1) }

        launch(database, targetName)
    }
}

private fun launch(database: GameDatabase, modName: String) {
    val logger = KLogging().logger

    val looseMods = database.looseMods
        .filter { it.name.toLowerCase().contains(modName.toLowerCase()) }
        .toList()
    val weaponMods = looseMods
        .mapNotNull { WeaponMod.fromLooseMod(it, database) }
        .sortedBy { it.effects.weapon!!.name }
        .toList()
    if (weaponMods.isEmpty()) {
        logger.warn { "No weapon mods by the name `$modName` were found." }
        return
    }

    val selection = WeaponSelection(database, modName, weaponMods)
    println(selection.createPage().toString())
}
