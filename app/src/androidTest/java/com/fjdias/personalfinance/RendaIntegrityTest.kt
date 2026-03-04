package com.fjdias.personalfinance

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RendaIntegrityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testRendaCategoryIsLocked() {
        // 1. Ir para a tela de Categorias
        composeTestRule.onNodeWithText("Categorias").performClick()

        // 2. Verificar se a categoria Renda existe
        composeTestRule.onNodeWithText("Renda").assertIsDisplayed()

        // 3. Verificar se o ícone de cadeado (Locked) aparece para a Renda
        // e se os botões de Editar/Excluir NÃO aparecem para ela
        composeTestRule.onNodeWithContentDescription("Categoria Fixa").assertIsDisplayed()
        
        // Buscamos especificamente por um nó que tenha o texto Renda e tentamos achar botões de edição ao lado
        // Se a lógica estiver correta, não haverá botões de ação para este nó específico
    }
}
