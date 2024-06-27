package com.hardcoreamature.tradingpersonalityapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PerfectLifeActivity : AppCompatActivity() {

    private lateinit var luxuryCondoSpinner: Spinner
    private lateinit var sportsCarsSpinner: Spinner
    private lateinit var vacationSpinner: Spinner
    private lateinit var fancyMealsSpinner: Spinner
    private lateinit var danceLessonsSpinner: Spinner
    private lateinit var nameBrandClothesSpinner: Spinner
    private lateinit var homeTheaterSpinner: Spinner
    private lateinit var maidServiceSpinner: Spinner
    private lateinit var lavishChristmasSpinner: Spinner
    private lateinit var lavishBirthdaySpinner: Spinner
    private lateinit var personalChefSpinner: Spinner
    private lateinit var personalTrainerSpinner: Spinner
    private lateinit var concertTicketsSpinner: Spinner
    private lateinit var charitySpinner: Spinner
    private lateinit var spaTreatmentsSpinner: Spinner
    private lateinit var flightLessonSpinner: Spinner
    private lateinit var limousineRidesSpinner: Spinner
    private lateinit var randomBillsSpinner: Spinner
    private lateinit var buyWhateverSpinner: Spinner

    private lateinit var luxuryCondoCost: EditText
    private lateinit var sportsCarsCost: EditText
    private lateinit var vacationCost: EditText
    private lateinit var fancyMealsCost: EditText
    private lateinit var danceLessonsCost: EditText
    private lateinit var nameBrandClothesCost: EditText
    private lateinit var homeTheaterCost: EditText
    private lateinit var maidServiceCost: EditText
    private lateinit var lavishChristmasCost: EditText
    private lateinit var lavishBirthdayCost: EditText
    private lateinit var personalChefCost: EditText
    private lateinit var personalTrainerCost: EditText
    private lateinit var concertTicketsCost: EditText
    private lateinit var charityCost: EditText
    private lateinit var spaTreatmentsCost: EditText
    private lateinit var flightLessonCost: EditText
    private lateinit var limousineRidesCost: EditText
    private lateinit var randomBillsCost: EditText
    private lateinit var buyWhateverCost: EditText

    private lateinit var calculateButton: Button
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfect_life)

        luxuryCondoSpinner = findViewById(R.id.spinner_luxury_condo)
        sportsCarsSpinner = findViewById(R.id.spinner_sports_cars)
        vacationSpinner = findViewById(R.id.spinner_vacation)
        fancyMealsSpinner = findViewById(R.id.spinner_fancy_meals)
        danceLessonsSpinner = findViewById(R.id.spinner_dance_lessons)
        nameBrandClothesSpinner = findViewById(R.id.spinner_name_brand_clothes)
        homeTheaterSpinner = findViewById(R.id.spinner_home_theater)
        maidServiceSpinner = findViewById(R.id.spinner_maid_service)
        lavishChristmasSpinner = findViewById(R.id.spinner_lavish_christmas)
        lavishBirthdaySpinner = findViewById(R.id.spinner_lavish_birthday)
        personalChefSpinner = findViewById(R.id.spinner_personal_chef)
        personalTrainerSpinner = findViewById(R.id.spinner_personal_trainer)
        concertTicketsSpinner = findViewById(R.id.spinner_concert_tickets)
        charitySpinner = findViewById(R.id.spinner_charity)
        spaTreatmentsSpinner = findViewById(R.id.spinner_spa_treatments)
        flightLessonSpinner = findViewById(R.id.spinner_flight_lesson)
        limousineRidesSpinner = findViewById(R.id.spinner_limousine_rides)
        randomBillsSpinner = findViewById(R.id.spinner_random_bills)
        buyWhateverSpinner = findViewById(R.id.spinner_buy_whatever)

        luxuryCondoCost = findViewById(R.id.edit_text_luxury_condo_cost)
        sportsCarsCost = findViewById(R.id.edit_text_sports_cars_cost)
        vacationCost = findViewById(R.id.edit_text_vacation_cost)
        fancyMealsCost = findViewById(R.id.edit_text_fancy_meals_cost)
        danceLessonsCost = findViewById(R.id.edit_text_dance_lessons_cost)
        nameBrandClothesCost = findViewById(R.id.edit_text_name_brand_clothes_cost)
        homeTheaterCost = findViewById(R.id.edit_text_home_theater_cost)
        maidServiceCost = findViewById(R.id.edit_text_maid_service_cost)
        lavishChristmasCost = findViewById(R.id.edit_text_lavish_christmas_cost)
        lavishBirthdayCost = findViewById(R.id.edit_text_lavish_birthday_cost)
        personalChefCost = findViewById(R.id.edit_text_personal_chef_cost)
        personalTrainerCost = findViewById(R.id.edit_text_personal_trainer_cost)
        concertTicketsCost = findViewById(R.id.edit_text_concert_tickets_cost)
        charityCost = findViewById(R.id.edit_text_charity_cost)
        spaTreatmentsCost = findViewById(R.id.edit_text_spa_treatments_cost)
        flightLessonCost = findViewById(R.id.edit_text_flight_lesson_cost)
        limousineRidesCost = findViewById(R.id.edit_text_limousine_rides_cost)
        randomBillsCost = findViewById(R.id.edit_text_random_bills_cost)
        buyWhateverCost = findViewById(R.id.edit_text_buy_whatever_cost)

        calculateButton = findViewById(R.id.button_calculate_perfect_life)
        resultTextView = findViewById(R.id.text_view_perfect_life_result)

        // Initialize the spinners with example data
        val options = arrayOf("Yes", "No")

        setUpSpinner(luxuryCondoSpinner, options)
        setUpSpinner(sportsCarsSpinner, options)
        setUpSpinner(vacationSpinner, options)
        setUpSpinner(fancyMealsSpinner, options)
        setUpSpinner(danceLessonsSpinner, options)
        setUpSpinner(nameBrandClothesSpinner, options)
        setUpSpinner(homeTheaterSpinner, options)
        setUpSpinner(maidServiceSpinner, options)
        setUpSpinner(lavishChristmasSpinner, options)
        setUpSpinner(lavishBirthdaySpinner, options)
        setUpSpinner(personalChefSpinner, options)
        setUpSpinner(personalTrainerSpinner, options)
        setUpSpinner(concertTicketsSpinner, options)
        setUpSpinner(charitySpinner, options)
        setUpSpinner(spaTreatmentsSpinner, options)
        setUpSpinner(flightLessonSpinner, options)
        setUpSpinner(limousineRidesSpinner, options)
        setUpSpinner(randomBillsSpinner, options)
        setUpSpinner(buyWhateverSpinner, options)

        calculateButton.setOnClickListener {
            calculatePerfectLifeCost()
        }
    }

    private fun setUpSpinner(spinner: Spinner, options: Array<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun calculatePerfectLifeCost() {
        var totalCost = 0.0

        if (luxuryCondoSpinner.selectedItem == "Yes") totalCost += luxuryCondoCost.text.toString().toDoubleOrNull() ?: 2000000.0
        if (sportsCarsSpinner.selectedItem == "Yes") totalCost += sportsCarsCost.text.toString().toDoubleOrNull() ?: 36000.0
        if (vacationSpinner.selectedItem == "Yes") totalCost += vacationCost.text.toString().toDoubleOrNull() ?: 40000.0
        if (fancyMealsSpinner.selectedItem == "Yes") totalCost += fancyMealsCost.text.toString().toDoubleOrNull() ?: 10400.0
        if (danceLessonsSpinner.selectedItem == "Yes") totalCost += danceLessonsCost.text.toString().toDoubleOrNull() ?: 3900.0
        if (nameBrandClothesSpinner.selectedItem == "Yes") totalCost += nameBrandClothesCost.text.toString().toDoubleOrNull() ?: 36000.0
        if (homeTheaterSpinner.selectedItem == "Yes") totalCost += homeTheaterCost.text.toString().toDoubleOrNull() ?: 10000.0
        if (maidServiceSpinner.selectedItem == "Yes") totalCost += maidServiceCost.text.toString().toDoubleOrNull() ?: 26000.0
        if (lavishChristmasSpinner.selectedItem == "Yes") totalCost += lavishChristmasCost.text.toString().toDoubleOrNull() ?: 20000.0
        if (lavishBirthdaySpinner.selectedItem == "Yes") totalCost += lavishBirthdayCost.text.toString().toDoubleOrNull() ?: 20000.0
        if (personalChefSpinner.selectedItem == "Yes") totalCost += personalChefCost.text.toString().toDoubleOrNull() ?: 7800.0
        if (personalTrainerSpinner.selectedItem == "Yes") totalCost += personalTrainerCost.text.toString().toDoubleOrNull() ?: 7800.0
        if (concertTicketsSpinner.selectedItem == "Yes") totalCost += concertTicketsCost.text.toString().toDoubleOrNull() ?: 6000.0
        if (charitySpinner.selectedItem == "Yes") totalCost += charityCost.text.toString().toDoubleOrNull() ?: 50000.0
        if (spaTreatmentsSpinner.selectedItem == "Yes") totalCost += spaTreatmentsCost.text.toString().toDoubleOrNull() ?: 5200.0
        if (flightLessonSpinner.selectedItem == "Yes") totalCost += flightLessonCost.text.toString().toDoubleOrNull() ?: 50000.0
        if (limousineRidesSpinner.selectedItem == "Yes") totalCost += limousineRidesCost.text.toString().toDoubleOrNull() ?: 5000.0
        if (randomBillsSpinner.selectedItem == "Yes") totalCost += randomBillsCost.text.toString().toDoubleOrNull() ?: 50000.0
        if (buyWhateverSpinner.selectedItem == "Yes") totalCost += buyWhateverCost.text.toString().toDoubleOrNull() ?: 100000.0

        resultTextView.text = getString(R.string.perfect_life_result, totalCost)
    }
}

