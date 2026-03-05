package com.fjdias.personalfinance

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FinanceBusinessLogicTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun scenario3_testManualExpenseInsertion() {
        composeTestRule.onNodeWithTag("NavTransactions").performClick()
        composeTestRule.onNodeWithTag("AddTransactionFAB").performClick()
        
        composeTestRule.onNodeWithTag("TransactionTitleField").performTextInput("Internet Fibra")
        composeTestRule.onNodeWithTag("TransactionAmountField").performTextInput("150.00")
        
        composeTestRule.onNodeWithTag("CategorySelectorButton").performClick()
        composeTestRule.onNodeWithTag("CategoryItem_Outros").performClick()
        
        composeTestRule.onNodeWithTag("SaveTransactionButton").performClick()
        
        composeTestRule.onNodeWithTag("TransactionItem_Internet Fibra").assertIsDisplayed()
    }

    @Test
    fun scenario5_testGlobalCategoryRenaming() {
        // Limpar dados para garantir estado conhecido
        composeTestRule.onNodeWithTag("DeleteAllButton").performClick()
        composeTestRule.onNodeWithTag("ConfirmDeleteButton").performClick()

        // 1. Criar categoria e transação
        composeTestRule.onNodeWithTag("NavCategories").performClick()
        composeTestRule.onNodeWithTag("NewCategoryField").performTextInput("Lazer Teste")
        composeTestRule.onNodeWithTag("AddCategoryButton").performClick()

        composeTestRule.onNodeWithTag("NavTransactions").performClick()
        composeTestRule.onNodeWithTag("AddTransactionFAB").performClick()
        composeTestRule.onNodeWithTag("TransactionTitleField").performTextInput("Cinema")
        composeTestRule.onNodeWithTag("TransactionAmountField").performTextInput("60.00")
        composeTestRule.onNodeWithTag("CategorySelectorButton").performClick()
        composeTestRule.onNodeWithTag("CategoryItem_Lazer Teste").performClick()
        composeTestRule.onNodeWithTag("SaveTransactionButton").performClick()

        // 2. Renomear
        composeTestRule.onNodeWithTag("NavCategories").performClick()
        composeTestRule.onNodeWithTag("EditCategoryButton_Lazer Teste").performClick()
        composeTestRule.onNodeWithTag("EditCategoryField").performTextReplacement("Diversão")
        composeTestRule.onNodeWithTag("SaveCategoryEditButton").performClick()

        // 3. Validar na lista de transações (usando a tag da categoria dentro do item)
        composeTestRule.onNodeWithTag("NavTransactions").performClick()
        composeTestRule.onNodeWithTag("TransactionItem_Cinema").assertIsDisplayed()
        composeTestRule.onNodeWithTag("TransactionCategoryName_Cinema", useUnmergedTree = true).assertTextEquals("Diversão")
    }

    @Test
    fun scenario6_testIncomeVsExpenseSummaryLogic() {
        // Limpar para garantir valores exatos
        composeTestRule.onNodeWithTag("DeleteAllButton").performClick()
        composeTestRule.onNodeWithTag("ConfirmDeleteButton").performClick()

        // Adicionar Renda
        composeTestRule.onNodeWithTag("AddTransactionFAB").performClick()
        composeTestRule.onNodeWithTag("TransactionTitleField").performTextInput("Salário")
        composeTestRule.onNodeWithTag("TransactionAmountField").performTextInput("5000")
        composeTestRule.onNodeWithTag("CategorySelectorButton").performClick()
        composeTestRule.onNodeWithTag("CategoryItem_Renda").performClick()
        composeTestRule.onNodeWithTag("SaveTransactionButton").performClick()

        // Adicionar Gasto
        composeTestRule.onNodeWithTag("AddTransactionFAB").performClick()
        composeTestRule.onNodeWithTag("TransactionTitleField").performTextInput("Aluguel")
        composeTestRule.onNodeWithTag("TransactionAmountField").performTextInput("1200")
        composeTestRule.onNodeWithTag("CategorySelectorButton").performClick()
        composeTestRule.onNodeWithTag("CategoryItem_Outros").performClick()
        composeTestRule.onNodeWithTag("SaveTransactionButton").performClick()

        composeTestRule.onNodeWithTag("NavSummary").performClick()

        // Validar usando TestTags e locale pt-BR definido na MainActivity
        composeTestRule.onNodeWithTag("SummaryIncome").assertTextEquals("R$ 5.000,00")
        composeTestRule.onNodeWithTag("SummaryExpenses").assertTextEquals("R$ 1.200,00")
    }
}
