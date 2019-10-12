package week3.tasks.algebraic

sealed trait Calculator

class Success(an : Int) extends Calculator
class Fail(message : String) extends Calculator



