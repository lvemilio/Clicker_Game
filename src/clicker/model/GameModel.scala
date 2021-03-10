package clicker.model

class GameModel {
  var gold: Double = 0
  var shovels:Double = 0
  var excavators:Double = 0
  var goldMines:Double = 0
  var shovelCost:Double = 10
  var excavatorCost:Double = 200
  var goldMineCost:Double = 1000
  var lastUpdateTime:Long = System.nanoTime()

  def getGameState:String={
    val gameState: String =
      """ {"username": "test",
                "gold":""" + gold.toString +
        """,
                "lastUpdateTime":""" + lastUpdateTime.toString +
        """,
                 "equipment":{
                    "shovel":{"id":"shovel", "name":"Shovel", "numberOwned":""" + shovels + ""","cost":"""+shovelCost +"""},
                    "excavator":{"id":"excavator","name":"Excavator","numberOwned":""" + excavators + ""","cost":"""+excavatorCost +"""},
                    "mine":{"id":"mine", "name":"Gold Mine", "numberOwned":""" + goldMines + ""","cost":"""+goldMineCost + """}
              }
            }
          """
    gameState
  }
  def passiveIncome(curTime:Long):Unit={
    val timeCounter:Double = (curTime - lastUpdateTime)/1000000000.0
    println("Current time counter:"+ timeCounter)
    gold += (timeCounter*goldMines*100) + (timeCounter*excavators*10)
    lastUpdateTime = System.nanoTime()
  }

}
