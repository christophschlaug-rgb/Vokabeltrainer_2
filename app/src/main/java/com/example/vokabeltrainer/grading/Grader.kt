package com.example.vokabeltrainer.grading

/**
 * Automatische Bewertung der Nutzereingabe.
 *
 * Regeln:
 * - Trim + lowercase
 * - Bei englischen Lösungen: führendes "to " ist optional (beidseitig)
 * - Eine der korrekten Übersetzungen genügt
 */
object Grader {

    enum class Direction { DE_TO_EN, EN_TO_DE }

    /**
     * @param direction Abfragerichtung
     * @param solutions Menge korrekter Lösungen (EN: meist 1, DE: oft mehrere)
     * @param userInput Rohe Nutzereingabe
     */
    fun check(direction: Direction, solutions: List<String>, userInput: String): Boolean {
        if (userInput.isBlank()) return false
        val normIn = if (direction == Direction.DE_TO_EN) normEn(userInput) else normDe(userInput)
        if (normIn.isEmpty()) return false
        return solutions.any { sol ->
            val normSol = if (direction == Direction.DE_TO_EN) normEn(sol) else normDe(sol)
            normSol == normIn
        }
    }

    private fun normEn(s: String): String {
        val t = s.trim().lowercase()
        return if (t.startsWith("to ")) t.substring(3).trim() else t
    }

    private fun normDe(s: String): String = s.trim().lowercase()

    // --- Optionale "fast richtig"-Option (nicht aktiv im MVP) ---
    // Aktiviere über Settings/Toggle, indem du check() durch checkWithTolerance ersetzt.
    fun checkWithTolerance(direction: Direction, solutions: List<String>, userInput: String): Boolean {
        if (check(direction, solutions, userInput)) return true
        val normIn = if (direction == Direction.DE_TO_EN) normEn(userInput) else normDe(userInput)
        if (normIn.length < 5) return false
        return solutions.any { sol ->
            val normSol = if (direction == Direction.DE_TO_EN) normEn(sol) else normDe(sol)
            levenshtein(normSol, normIn) <= 1
        }
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)
        for (i in 1..a.length) {
            curr[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = minOf(curr[j - 1] + 1, prev[j] + 1, prev[j - 1] + cost)
            }
            System.arraycopy(curr, 0, prev, 0, curr.size)
        }
        return prev[b.length]
    }
}
