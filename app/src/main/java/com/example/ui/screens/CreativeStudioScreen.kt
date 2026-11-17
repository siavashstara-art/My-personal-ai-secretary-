package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.AssistantPersona
import com.example.model.StudioSection
import com.example.ui.components.StatusPill
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioDropdownSelector

@Composable
fun CreativeStudioScreen(
    section: StudioSection,
    personas: List<AssistantPersona>,
    selectedPersona: AssistantPersona,
    charName: String,
    charAge: Int,
    charStyle: String,
    charCategory: String,
    tryOnModel: String,
    tryOnClothing: String,
    bridalGownStyle: String,
    groomSuitStyle: String,
    sceneType: String,
    lightingSetup: String,
    onSelectPersona: (AssistantPersona) -> Unit,
    onSelectSection: (StudioSection) -> Unit,
    onCharNameChange: (String) -> Unit,
    onCharAgeChange: (Int) -> Unit,
    onCharStyleChange: (String) -> Unit,
    onCharCategoryChange: (String) -> Unit,
    onTryOnModelChange: (String) -> Unit,
    onTryOnClothingChange: (String) -> Unit,
    onBridalGownChange: (String) -> Unit,
    onGroomSuitChange: (String) -> Unit,
    onSceneTypeChange: (String) -> Unit,
    onLightingSetupChange: (String) -> Unit,
    onSendPromptToAi: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (section != StudioSection.Charter) {
        BackHandler {
            onSelectSection(StudioSection.Charter)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Horizontal Module Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chips = listOf(
                StudioSection.Charter to "Charter",
                StudioSection.ModuleA to "A. Character",
                StudioSection.ModuleB to "B. Try-On",
                StudioSection.ModuleC to "C. Fashion",
                StudioSection.ModuleD to "D. Wedding",
                StudioSection.ModuleE to "E. Scene",
                StudioSection.ModuleF to "F. Image/Video",
                StudioSection.TechCheck to "Tech Check"
            )
            chips.forEach { (item, label) ->
                FilterChip(
                    selected = section == item,
                    onClick = { onSelectSection(item) },
                    label = { Text(label) },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("studio_tab_${item.name.lowercase()}")
                )
            }
        }

        if (section != StudioSection.Charter) {
            OutlinedButton(
                onClick = { onSelectSection(StudioSection.Charter) },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("studio_back_charter_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Charter", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Back to Charter Overview")
            }
        }

        when (section) {
            StudioSection.Charter -> CharterOverviewContent(onSelectSection = onSelectSection)
            StudioSection.ModuleA -> ModuleAContent(
                personas = personas,
                selectedPersona = selectedPersona,
                charName = charName,
                charAge = charAge,
                charStyle = charStyle,
                charCategory = charCategory,
                onSelectPersona = onSelectPersona,
                onCharNameChange = onCharNameChange,
                onCharAgeChange = onCharAgeChange,
                onCharStyleChange = onCharStyleChange,
                onCharCategoryChange = onCharCategoryChange,
                onGeneratePrompt = {
                    val prompt = "Generate detailed character prompt specifications for fictional adult character $charName, age $charAge, style $charStyle, category $charCategory, silhouette ${selectedPersona.silhouetteType}, voice ${selectedPersona.voiceLabel}, personality ${selectedPersona.personalitySummary}."
                    onSendPromptToAi("Module A — Fictional Character Studio", prompt)
                }
            )
            StudioSection.ModuleB -> ModuleBContent(
                tryOnModel = tryOnModel,
                tryOnClothing = tryOnClothing,
                onTryOnModelChange = onTryOnModelChange,
                onTryOnClothingChange = onTryOnClothingChange,
                onEvaluate = {
                    val prompt = "Analyze virtual try-on workflow for placing \"$tryOnClothing\" onto \"$tryOnModel\", detailing ControlNet depth/pose guidelines and texture preservation strategies."
                    onSendPromptToAi("Module B — Virtual Clothing Try-On", prompt)
                }
            )
            StudioSection.ModuleC -> ModuleCContent(
                onGenerateIdeas = { title ->
                    val prompt = "Create tasteful editorial catalog concepts and lighting directives for $title within the Personal AI Creative Studio."
                    onSendPromptToAi("Module C — Fashion & Editorial Studio", prompt)
                }
            )
            StudioSection.ModuleD -> ModuleDContent(
                bridalGownStyle = bridalGownStyle,
                groomSuitStyle = groomSuitStyle,
                onBridalGownChange = onBridalGownChange,
                onGroomSuitChange = onGroomSuitChange,
                onGeneratePrompt = {
                    val prompt = "Create a detailed co-display prompt for a bride in a $bridalGownStyle and groom in a $groomSuitStyle, staged in a luxurious botanical conservatory for wedding visualization."
                    onSendPromptToAi("Module D — Wedding & Formal Wear Studio", prompt)
                }
            )
            StudioSection.ModuleE -> ModuleEContent(
                sceneType = sceneType,
                lightingSetup = lightingSetup,
                onSceneTypeChange = onSceneTypeChange,
                onLightingSetupChange = onLightingSetupChange,
                onGenerateScript = {
                    val prompt = "Write a cinematic scene production script and image generation prompt for: $sceneType with $lightingSetup. Include camera angle, character emotions, and background ambience."
                    onSendPromptToAi("Module E — Scene & Story Studio", prompt)
                }
            )
            StudioSection.ModuleF -> ModuleFContent()
            StudioSection.TechCheck -> TechCheckContent()
        }
    }
}

@Composable
private fun CharterOverviewContent(onSelectSection: (StudioSection) -> Unit) {
    StudioCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PROJECT INITIALIZATION SCRIPT V1.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Personal AI Creative Studio",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            StatusPill(
                label = "PRIORITY: NONE",
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text("1. Project Purpose", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = "Create a personal AI-powered creative studio for fictional character creation, virtual clothing try-on, fashion visualization, image editing, and creative scene production.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Reserved for future personal use, experimentation, and entertainment. Not intended for commercial release or monetization.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                    Text("3. Security & Privacy Principles", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                listOf(
                    "Local-first architecture whenever practical",
                    "No automatic uploading of personal images",
                    "No mandatory paid subscriptions or API key exposure",
                    "Fictional characters remain strictly tasteful and compliant"
                ).forEach { rule ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(15.dp))
                        Text(rule, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    Text(
        text = "Core Studio Modules Roadmap",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )

    val roadmapModules: List<Triple<StudioSection, String, Pair<String, ImageVector>>> = listOf(
        Triple(StudioSection.ModuleA, "Module A: Fictional Character Studio", "Create fictional character profiles, outfits, poses, backgrounds, and visual styles." to Icons.Filled.Person),
        Triple(StudioSection.ModuleB, "Module B: Virtual Clothing Try-On", "Input clothing product & model reference while preserving colors, lace, and textures." to Icons.Filled.Checkroom),
        Triple(StudioSection.ModuleC, "Module C: Fashion & Editorial Studio", "Presentation of couture, resort wear, and promotional lookbook concepts." to Icons.Filled.Diamond),
        Triple(StudioSection.ModuleD, "Module D: Wedding & Formal Wear", "Virtual wedding dress & groom suit fitting and co-display visualization." to Icons.Filled.Favorite),
        Triple(StudioSection.ModuleE, "Module E: Scene & Story Studio", "Create visual scenes: wedding hall entrance, reception, sunset terrace, and lighting." to Icons.Filled.Movie),
        Triple(StudioSection.ModuleF, "Module F: Basic Image & Video Tools", "Image collections, basic editing, subtitle overlays, and short video feasibility." to Icons.Filled.Layers)
    )

    roadmapModules.forEach { (target, title, descIcon) ->
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable { onSelectSection(target) }
                .testTag("roadmap_card_${target.name.lowercase()}"),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(descIcon.second, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(descIcon.first, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("Explore →", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    StudioCard(borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Text("7. Project Priorities & Execution Order", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        listOf(
            "1. Complete existing unfinished applications.",
            "2. Finish AFRINA Ad Studio.",
            "3. Audit FaceGuard / Roozegar functionality.",
            "4. Audit and validate SalonYar and other existing applications.",
            "5. Resolve FarshBazaar release packaging.",
            "6. Verify APK/AAB generation, signing, installation, and release readiness."
        ).forEach { step ->
            Text(step, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ModuleAContent(
    personas: List<AssistantPersona>,
    selectedPersona: AssistantPersona,
    charName: String,
    charAge: Int,
    charStyle: String,
    charCategory: String,
    onSelectPersona: (AssistantPersona) -> Unit,
    onCharNameChange: (String) -> Unit,
    onCharAgeChange: (Int) -> Unit,
    onCharStyleChange: (String) -> Unit,
    onCharCategoryChange: (String) -> Unit,
    onGeneratePrompt: () -> Unit
) {
    StudioCard {
        Text("Module A — Fictional Character Studio", style = MaterialTheme.typography.titleLarge)
        Text("انتخاب سریع از بین ۳ بانوی استودیو (۲۷ تا ۳۷ سال):", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            personas.forEach { persona ->
                FilterChip(
                    selected = persona.id == selectedPersona.id,
                    onClick = { onSelectPersona(persona) },
                    label = { Text("${persona.persianName} (${persona.age})") },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
            }
        }
        OutlinedTextField(
            value = charName,
            onValueChange = onCharNameChange,
            label = { Text("Character Name / Identifier") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("char_name_input")
        )
        OutlinedTextField(
            value = charAge.toString(),
            onValueChange = { text -> text.toIntOrNull()?.let(onCharAgeChange) },
            label = { Text("Age (Fictional Adult)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("char_age_input")
        )
        StudioDropdownSelector(
            label = "Visual Art Style",
            selectedValue = charStyle,
            options = listOf("Cinematic Editorial", "High Fashion Magazine", "Studio Portrait Soft Lighting", "Dramatic Noir"),
            onOptionSelected = onCharStyleChange,
            testTag = "char_style_dropdown"
        )
        StudioDropdownSelector(
            label = "Clothing Category",
            selectedValue = charCategory,
            options = listOf("Casual Wear", "Formal & Evening Wear", "Resort & Cruise Wear", "Couture & Editorial"),
            onOptionSelected = onCharCategoryChange,
            testTag = "char_category_dropdown"
        )
        Button(
            onClick = onGeneratePrompt,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("char_generate_ai_button")
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Generate Character Prompt with Gemini")
        }
    }

    StudioCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusPill(
                label = "STABLE SEED READY",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Image(
                painter = painterResource(id = selectedPersona.portraitRes),
                contentDescription = charName,
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )
            Text(charName, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${selectedPersona.silhouetteType} • ${selectedPersona.voiceLabel}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Fictional character profile configured for $charCategory under $charStyle. Age $charAge • ${selectedPersona.personalitySummary}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModuleBContent(
    tryOnModel: String,
    tryOnClothing: String,
    onTryOnModelChange: (String) -> Unit,
    onTryOnClothingChange: (String) -> Unit,
    onEvaluate: () -> Unit
) {
    StudioCard {
        Text("Module B — Virtual Clothing Try-On Studio", style = MaterialTheme.typography.titleLarge)
        StudioDropdownSelector(
            label = "Target Model / Character Base",
            selectedValue = tryOnModel,
            options = listOf("Model #04 (Studio Fit)", "Model #09 (Editorial Pose)", "Elena Vance (Character A)"),
            onOptionSelected = onTryOnModelChange,
            testTag = "tryon_model_dropdown"
        )
        OutlinedTextField(
            value = tryOnClothing,
            onValueChange = onTryOnClothingChange,
            label = { Text("Clothing Product Description / Image URL") },
            modifier = Modifier.fillMaxWidth().testTag("tryon_clothing_input")
        )
        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Accuracy & Detail Preservation Rules:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                Text("• Preserve product colors, cuts, straps, and stitching.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("• Maintain lace patterns and fabric drape.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("• Avoid distortion of garment logos and hardware details.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Button(
            onClick = onEvaluate,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("tryon_evaluate_ai_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Evaluate Try-On Feasibility with Gemini")
        }
    }
}

@Composable
private fun ModuleCContent(onGenerateIdeas: (String) -> Unit) {
    StudioCard {
        Text("Module C — Fashion & Editorial Studio", style = MaterialTheme.typography.titleLarge)
        listOf(
            "Couture & Evening Apparel" to "Virtual presentation of tailored evening gowns, silk silhouettes, and structured editorial garments.",
            "Resort & Cruise Collections" to "Poolside and coastal golden-hour lighting simulations for seasonal resort catalogs.",
            "Product-Cover Concepts" to "High-end promotional lookbook concepts using fictional studio models for digital portfolios."
        ).forEach { (title, desc) ->
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(
                        onClick = { onGenerateIdeas(title) },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Generate Ideas →")
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleDContent(
    bridalGownStyle: String,
    groomSuitStyle: String,
    onBridalGownChange: (String) -> Unit,
    onGroomSuitChange: (String) -> Unit,
    onGeneratePrompt: () -> Unit
) {
    StudioCard {
        Text("Module D — Wedding & Formal Wear Studio", style = MaterialTheme.typography.titleLarge)
        StudioDropdownSelector(
            label = "Bridal Gown Style",
            selectedValue = bridalGownStyle,
            options = listOf("A-Line Lace Chantilly Gown", "Ballgown with Cathedral Train", "Sleek Minimalist Crepe Slip"),
            onOptionSelected = onBridalGownChange,
            testTag = "bridal_gown_dropdown"
        )
        StudioDropdownSelector(
            label = "Groom Suit Style",
            selectedValue = groomSuitStyle,
            options = listOf("Tailored Navy Tuxedo with Satin Lapel", "Classic Black Three-Piece Suit", "Modern Charcoal Slim Fit"),
            onOptionSelected = onGroomSuitChange,
            testTag = "groom_suit_dropdown"
        )
        Button(
            onClick = onGeneratePrompt,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("wedding_generate_ai_button")
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Generate Wedding Co-Display Prompt")
        }
    }
}

@Composable
private fun ModuleEContent(
    sceneType: String,
    lightingSetup: String,
    onSceneTypeChange: (String) -> Unit,
    onLightingSetupChange: (String) -> Unit,
    onGenerateScript: () -> Unit
) {
    StudioCard {
        Text("Module E — Scene & Story Studio", style = MaterialTheme.typography.titleLarge)
        StudioDropdownSelector(
            label = "Scene Concept",
            selectedValue = sceneType,
            options = listOf(
                "Wedding Reception Grand Entrance",
                "Romantic Couple Dancing Under Chandeliers",
                "Couple Entering Vintage Wedding Car",
                "Cinematic Fictional Sunset Terrace"
            ),
            onOptionSelected = onSceneTypeChange,
            testTag = "scene_type_dropdown"
        )
        StudioDropdownSelector(
            label = "Lighting & Environment",
            selectedValue = lightingSetup,
            options = listOf(
                "Warm Golden Hour & Soft Rim Light",
                "Dramatic Ballroom Chandeliers & Bokeh",
                "Moody Cinematic Noir Shadows"
            ),
            onOptionSelected = onLightingSetupChange,
            testTag = "lighting_setup_dropdown"
        )
        Button(
            onClick = onGenerateScript,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("scene_generate_ai_button")
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Generate Scene Script with Gemini")
        }
    }
}

@Composable
private fun ModuleFContent() {
    StudioCard {
        Text("Module F — Basic Image & Video Tools", style = MaterialTheme.typography.titleLarge)
        listOf(
            "Image Collections" to "Organize generated characters, outfits, and scene variants into private tag-based collections.",
            "Basic Image Editing" to "Crop, color grading, exposure adjustment, and retouching controls for final renders.",
            "Short Video Creation" to "Explore image-to-video motion generation subject to technical feasibility and hardware costs."
        ).forEach { (title, desc) ->
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary)
                    Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun TechCheckContent() {
    StudioCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("Technical Evaluation Factors (Rule 4)", style = MaterialTheme.typography.titleMedium)
        }
        listOf(
            "Virtual Try-On Quality: Requires ControlNet & garment-warping pipelines to preserve textures accurately.",
            "Character Identity Consistency: Requires LoRA or InstantID / IP-Adapter integration for stable face generation.",
            "Image-to-Video Feasibility: High VRAM requirements; evaluate as open-source models mature.",
            "Android Compatibility: Quantized local runtimes vs cloud Firebase AI Logic streaming."
        ).forEach { factor ->
            Text("• $factor", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    StudioCard {
        Text("Future Activation Procedure (Rule 8)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary)
        listOf(
            "1. Review project charter and current priorities.",
            "2. Audit available open-source technologies and local-first runtimes.",
            "3. Select the simplest feasible architecture.",
            "4. Build one limited proof of concept.",
            "5. Test output quality and device compatibility.",
            "6. Decide whether further development is justified."
        ).forEach { step ->
            Text(step, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
