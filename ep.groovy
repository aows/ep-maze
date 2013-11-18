@Grapes([
@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.6' ),
@GrabExclude('xml-apis:xml-apis'),
@GrabExclude('org.codehaus.groovy:groovy')
])

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseDecorator
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

enum CellStatus { UNEXPLORED, BLOCKED }
enum Directions { NORTH, EAST, WEST, SOUTH }
class Cell {
	int x
	int y
	def directions
	def note
}
http = new HTTPBuilder('http://www.epdeveloperchallenge.com/')
mazeGuid = ''
found = false
currentCell = null
cells = []

http.request(POST, JSON) {
    uri.path = '/api/init'    

    response.success = { resp, json ->
    	mazeGuid = json.currentCell.mazeGuid
    	currentCell = new Cell()
    	currentCell.x = json.currentCell.x
    	currentCell.y = json.currentCell.y
    	currentCell.directions = [ (Directions.NORTH): json.currentCell.north,
    							   (Directions.EAST): json.currentCell.east,
    							   (Directions.SOUTH): json.currentCell.south,
    							   (Directions.WEST): json.currentCell.west ]
    	currentCell.note = json.currentCell.note
    	println "${json}"
    }

    response.failure = { resp ->
        println "Request failed with status ${resp.status}"
    }
}

while(true) {
	print "\rcurrent position: [${currentCell.x}, ${currentCell.y}]  || unexplored: ${cells.size}"
	moved = false
	if (currentCell.directions.get(Directions.NORTH) == 'UNEXPLORED') {
		cells.add(currentCell)
	} 
	if (currentCell.directions.get(Directions.EAST) == 'UNEXPLORED') {
		cells.add(currentCell)
	} 
	if (currentCell.directions.get(Directions.SOUTH) == 'UNEXPLORED') {
		cells.add(currentCell)
	}
	if (currentCell.directions.get(Directions.WEST) == 'UNEXPLORED') {
		cells.add(currentCell)
	}
	move()
	if (found) { break }
	if (!moved) { break }
}

def move() {
	def cellToMove = cells.pop()
	if (currentCell.x != cellToMove.x || currentCell.y != cellToMove.y) {
		jump(cellToMove.x, cellToMove.y)
	}
	if (currentCell.directions.get(Directions.NORTH) == 'UNEXPLORED') {
		move(Directions.NORTH)
		return
	} 
	if (currentCell.directions.get(Directions.EAST) == 'UNEXPLORED') {
		move(Directions.EAST)
		return
	} 
	if (currentCell.directions.get(Directions.SOUTH) == 'UNEXPLORED') {
		move(Directions.SOUTH)
		return
	}
	if (currentCell.directions.get(Directions.WEST) == 'UNEXPLORED') {
		move(Directions.WEST)
		return
	}
}

def jump(x, y) {
	http.request(POST, JSON) {
    	uri.path = '/api/jump' 
    	uri.query = [ 'mazeGuid': mazeGuid, 'x': x, 'y': y]   

    	response.success = { resp, json ->
    		mazeGuid = json.currentCell.mazeGuid
    		currentCell = new Cell()
    		currentCell.x = json.currentCell.x
    		currentCell.y = json.currentCell.y
    		currentCell.directions = [ (Directions.NORTH): json.currentCell.north,
    								   (Directions.EAST): json.currentCell.east,
    								   (Directions.SOUTH): json.currentCell.south,
    								   (Directions.WEST): json.currentCell.west ]
    	}

    	response.failure = { resp ->
        	println "Request failed with status ${resp.status}"
    	}
	}	
}
def move(direction) {
	http.request(POST, JSON) {
    	uri.path = '/api/move' 
    	uri.query = [ 'mazeGuid': mazeGuid, 'direction': direction]   

    	response.success = { resp, json ->
    		moved = true
    		if (json.currentCell.atEnd) {
    			found = true
    			println json
    		}
    		mazeGuid = json.currentCell.mazeGuid
    		currentCell = new Cell()
    		currentCell.x = json.currentCell.x
    		currentCell.y = json.currentCell.y
    		currentCell.directions = [ (Directions.NORTH): json.currentCell.north,
    								   (Directions.EAST): json.currentCell.east,
    								   (Directions.SOUTH): json.currentCell.south,
    								   (Directions.WEST): json.currentCell.west ]
    	}

    	response.failure = { resp ->
        	println "Request failed with status ${resp.status}"
    	}
	}
}