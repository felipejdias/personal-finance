package com.fjdias.personalfinance

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Implementação dos Cenários 3, 5 e 6 de TEST_SCENARIOS.md
 */
@RunWith(AndroidJUnit4::class)
class FinanceBusinessLogicTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun scenario3_testManualExpenseInsertion() {
        // Contexto: Aba de Transações
        composeTestRule.onNodeWithText("Transações").performClick()

        // Ação: Inserir gasto manual
        composeTestRule.onNodeWithContentDescription("Adicionar Transação").performClick()
        composeTestRule.onNodeWithText("Título").performTextInput("Internet Fibra")
        composeTestRule.onNodeWithText("Valor (R$)").performTextInput("150.00")
        
        // Clicar no botão de categoria no diálogo
        composeTestRule.onNode(hasText("Categoria:", substring = true)).performClick()
        
        // Selecionar "Outros" (garantido existir como fallback)
        // Usamos hasAnyAncestor para garantir que clicamos no item do menu e não em um filtro da tela principal
        composeTestRule.onNode(hasText("Outros") and hasAnyAncestor(isPopup())).performClick()
        
        composeTestRule.onNodeWithText("Salvar").performClick()

        // Resultado Esperado: Deve aparecer na lista
        composeTestRule.onNodeWithText("Internet Fibra").assertIsDisplayed()
    }

    @Test
    fun scenario5_testGlobalCategoryRenaming() {
        // 1. Criar categoria customizada primeiro
        composeTestRule.onNodeWithText("Categorias").performClick()
        composeTestRule.onNodeWithText("Nova categoria").performTextInput("Lazer Teste")
        composeTestRule.onNodeWithContentDescription("Adicionar").performClick()

        // 2. Criar transação na categoria "Lazer Teste"
        composeTestRule.onNodeWithText("Transações").performClick()
        composeTestRule.onNodeWithContentDescription("Adicionar Transação").performClick()
        composeTestRule.onNodeWithText("Título").performTextInput("Cinema")
        composeTestRule.onNodeWithText("Valor (R$)").performTextInput("60.00")
        composeTestRule.onNode(hasText("Categoria:", substring = true)).performClick()
        composeTestRule.onNode(hasText("Lazer Teste") and hasAnyAncestor(isPopup())).performClick()
        composeTestRule.onNodeWithText("Salvar").performClick()

        // 3. Ir para Categorias e Renomear "Lazer Teste" para "Diversão"
        composeTestRule.onNodeWithText("Categorias").performClick()
        // Clicar no botão editar da categoria Lazer Teste
        composeTestRule.onNode(hasAnyAncestor(hasText("Lazer Teste")) and hasContentDescription("Editar")).performClick()
        
        // Localizar o TextField de edição (que tem o texto atual)
        composeTestRule.onNode(hasText("Lazer Teste") and hasSetTextAction()).performTextReplacement("Diversão")
        composeTestRule.onNodeWithContentDescription("Salvar").performClick()

        // 4. Voltar para Transações e verificar se a categoria mudou no item
        composeTestRule.onNodeWithText("Transações").performClick()
        composeTestRule.onNodeWithText("Cinema").assertIsDisplayed()
        composeTestRule.onNodeWithText("Diversão").assertIsDisplayed()
    }

    @Test
    fun scenario6_testIncomeVsExpenseSummaryLogic() {
        // Garantir transações de Renda e Gastos
        composeTestRule.onNodeWithText("Transações").performClick()
        
        // Adicionar Renda
        composeTestRule.onNodeWithContentDescription("Adicionar Transação").performClick()
        composeTestRule.onNodeWithText("Título").performTextInput("Salário")
        composeTestRule.onNodeWithText("Valor (R$)").performTextInput("5000.00")
        composeTestRule.onNode(hasText("Categoria:", substring = true)).performClick()
        // Selecionar Renda no menu suspenso
        composeTestRule.onNode(hasText("Renda") and hasAnyAncestor(isPopup())).performClick()
        composeTestRule.onNodeWithText("Salvar").performClick()

        // Adicionar Gasto
        composeTestRule.onNodeWithContentDescription("Adicionar Transação").performClick()
        composeTestRule.onNodeWithText("Título").performTextInput("Aluguel")
        composeTestRule.onNodeWithText("Valor (R$)").performTextInput("1200.00")
        composeTestRule.onNode(hasText("Categoria:", substring = true)).performClick()
        // Selecionar Outros no menu suspenso
        composeTestRule.onNode(hasText("Outros") and hasAnyAncestor(isPopup())).performClick()
        composeTestRule.onNodeWithText("Salvar").performClick()

        // Ir para Resumo
        composeTestRule.onNodeWithText("Resumo").performClick()

        // Resultado Esperado: Renda em Entradas, Aluguel em Saídas
        // Usamos useUnmergedTree para encontrar os textos dentro dos SummaryItems se necessário
        composeTestRule.onNodeWithText("R$ 5000,00").assertIsDisplayed() // Entradas
        composeTestRule.onNodeWithText("R$ 1200,00").assertIsDisplayed() // Saídas
    }
}
