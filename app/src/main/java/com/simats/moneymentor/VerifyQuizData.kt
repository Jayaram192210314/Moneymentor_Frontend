package com.simats.moneymentor

import com.simats.moneymentor.data.QuizRepository

fun main() {
    var allPassed = true
    for (i in 1..16) {
        val quiz = QuizRepository.getQuizByModuleId(i)
        if (quiz == null) {
            println("Module $i: FAILED (Quiz is null)")
            allPassed = false
            continue
        }
        
        val count = quiz.questions.size
        if (count != 10) {
            println("Module $i: FAILED (Has $count questions instead of 10)")
            allPassed = false
        } else {
            println("Module $i: PASSED (10 questions)")
            // Basic sanity check for each question
            quiz.questions.forEachIndexed { index, q ->
                if (q.question.isBlank() || q.options.size != 4 || q.correctAnswerIndex < 0 || q.correctAnswerIndex >= 4) {
                    println("  - Question $index: FAILED Sanity Check")
                    allPassed = false
                }
            }
        }
    }
    
    if (allPassed) {
        println("\nALL TESTS PASSED!")
    } else {
        println("\nSOME TESTS FAILED!")
    }
}
