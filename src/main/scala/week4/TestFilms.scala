package week4

case class Film( name: String, yearOfRelease: Int, imdbRating: Double)
case class Director( firstName: String, lastName: String, yearOfBirth: Int, films: Seq[Film])


object Test extends App {

  val memento = new Film("Memento", 2000, 8.5)
  val darkKnight = new Film("Dark Knight", 2008, 9.0)
  val inception = new Film("Inception", 2010, 8.8)
  val highPlainsDrifter = new Film("High Plains Drifter", 1973, 7.7)
  val outlawJoseyWales = new Film("The Outlaw Josey Wales", 1976, 7.9)
  val unforgiven = new Film("Unforgiven", 1992, 8.3)
  val granTorino = new Film("Gran Torino", 2008, 8.2)
  val invictus = new Film("Invictus", 2009, 7.4)
  val predator = new Film("Predator", 1987, 7.9)
  val dieHard = new Film("Die Hard", 1988, 8.3)
  val huntForRedOctober = new Film("The Hunt for Red October", 1990, 7.6)
  val thomasCrownAffair = new Film("The Thomas Crown Affair", 1999, 6.8)

  val eastwood = new Director("Clint", "Eastwood", 1930, Seq(highPlainsDrifter, outlawJoseyWales, unforgiven, granTorino, invictus))
  val mcTiernan = new Director("John", "McTiernan", 1951, Seq(predator, dieHard, huntForRedOctober, thomasCrownAffair))
  val nolan = new Director("Christopher", "Nolan", 1970, Seq(memento, darkKnight, inception))
  val someGuy = new Director("Just", "Some Guy", 1990, Seq())

  val directors = Seq(eastwood, mcTiernan, nolan, someGuy)

  def byNumberOfFilms(numberOfFilms: Int): Seq[Director] = {
    directors.filter(directors => directors.films.size > numberOfFilms)
  }

  //println(byNumberOfFilms((4)))

  def byYear(year: Int): Seq[Director] = {
    directors.filter(directors => directors.yearOfBirth < year)
  }

  //println(byYear(1970))

  def byYearAndNumberOfFilms(year: Int, numberOfFilms: Int): Seq[Director] = {
    directors.filter(directors => directors.yearOfBirth < year && directors.films.size > numberOfFilms)
  }

  //println(byYearAndNumberOfFilms(3, 1930))


  // TODO: sortWith
  def byAscending(ascending: Boolean = true) = {
    directors.sortBy(directors => -directors.yearOfBirth)
  }

  //println(byAscending())


  def filmsOfNolan() ={
    //nolan.films.map(f => f.name)
    nolan.films.map(_.name)
  }

  //println(filmsOfNolan())

  def cinephile() = {
    //directors.map(directors => directors.films.map(film => film.name))
    directors.flatMap(_.films.map(_.name))
    //directors.flatten(_.films.map(_.name)).toList
  }

  //println(cinephile)


  def toMcTiernan(): Int = {
    mcTiernan.films.map(_.yearOfRelease).min
  }

  //println(toMcTiernan())

  def byFilmsRating() = {
    directors.flatMap(_.films.sortBy(film => film.imdbRating))
  }

  //println(byFilmsRating())

  def byDirectorsAvgScore() =  {
    directors.flatten(_.films).map(_.imdbRating).sum / directors.flatten(_.films).length
  }

  //println(byDirectorsAvgScore())*/

  def tonightListings() = {
    directors.foreach(director => director.films.foreach(film => println(s"Tonight only! ${film.name} NAME by ${director.lastName}!")))
  }

  //println(tonightListings())


  def fromTheArchives() = {
      directors.flatMap(_.films).map(_.imdbRating).min
      //directors.map(_.films.map(_.imdbRating))
  }

  println(fromTheArchives())

}
