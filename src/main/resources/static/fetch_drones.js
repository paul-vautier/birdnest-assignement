'use strict';
window.addEventListener('load', ()=> {
    /**
     * Adds the drone data to the list
     * @param {{serialNumber : string, pilotId : string, firstName : string, lastName : string,  latestObservation : string, phoneNumber : string, phoneNumber : string, createdDt : string, email : string, closestDistance : number, }[]} pilotList
     */
    function updateDroneData(pilotList) {
        let tbody = document.querySelector('#drones-table tbody');
        while (tbody.children.length > 0) {
            tbody.removeChild(tbody.children.item(tbody.children.length - 1));
        }

        pilotList.forEach((drone, index)=> {
            if (drone == null) {
                return;
            }
            tbody.innerHTML+= `
                <th scope="row"> ${index + 1}</td>
                <td>${drone.lastName} ${drone.firstName}</td>
                <td>${Math.round(drone.closestDistance * 100) / 100}</td>
                <td>${drone.latestObservation}</td>
                <td>${drone.phoneNumber}</td>
                <td><a href="mailto:${drone.email}">${drone.email}</a></td>
            `
        });
        drawMap(pilotList);
    }


    /**
     * Adds the drone data to the map
     * @param {{serialNumber : string, pilotId : string, firstName : string, lastName : string,  latestObservation : string, phoneNumber : string, phoneNumber : string, createdDt : string, email : string, closestDistance : number, }[]} pilotList
     */
    function drawMap(pilots) {
        const canvas = document.getElementById("map");
        const context = canvas.getContext("2d");
        canvas.height = window.innerHeight / 2;
        canvas.width = window.innerWidth / 2;
        const padding = 10;
        const centerPos = {x: canvas.width / 2, y: canvas.height / 2};
        const radius = Math.min(centerPos.x, centerPos.y) - padding;

        context.fillStyle = '#FFFFFF';
        context.fillRect(0, 0, canvas.width, canvas.height);
        context.beginPath();
        context.arc(centerPos.x, centerPos.y, radius, 0, 2 * Math.PI, false);
        context.strokeStyle = '#000000';
        context.stroke();

        pilots.forEach(drone => {
            if (drone == null) {
                return;
            }
            console.log({x : drone.px, y: drone.py})
            let pos = positionToCanvasPosition(drone.px, drone.py, padding, canvas);
            context.beginPath();
            context.arc(pos.x, pos.y, 5, 0, 2 * Math.PI, false);
            context.strokeStyle = '#FF0000';
            context.fillStyle = '#FF0000';
            context.fill();
        });
    }

    function positionToCanvasPosition(x, y, padding, canvas) {
        return {x: padding + (x/500000)*(canvas.height - padding), y :padding + (y/500000)*(canvas.width - padding)};
    }
    if (initData) {
        updateDroneData(initData);
    }

    let socket = new SockJS('/gs-guide-websocket');
    let stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, function () {
        stompClient.subscribe('/drones', function (message) {
            updateDroneData(JSON.parse(message.body));
        });
    });
});
