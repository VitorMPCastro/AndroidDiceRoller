package com.example.androiddiceroller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val diceViewModel: DiceViewModel = remember { DiceViewModel() }

    NavHost(navController, startDestination = ScreenRoutes.DICE_ROLLER) {
        composable(ScreenRoutes.DICE_ROLLER) {
            DiceRollerScreen(
                diceViewModel = diceViewModel,
                onCreateDie = { navController.navigate(ScreenRoutes.CREATE_DIE) },
                onManageDice = { navController.navigate(ScreenRoutes.MANAGE_DICE) },
                switchToScreenMultiDiceRoller = { navController.navigate(ScreenRoutes.MULTI_DICE_ROLL) }
            )
        }
        composable(ScreenRoutes.CREATE_DIE) {
            CreateDieScreen(
                navController = navController,
                diceViewModel = diceViewModel
            )
        }

        composable(ScreenRoutes.MANAGE_DICE) {
            ManageDiceScreen(
                navController = navController,
                diceViewModel = diceViewModel
            )
        }
    }
}