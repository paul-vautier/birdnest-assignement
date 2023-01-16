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
     * @param {{px: number, py:number, serialNumber : string, pilotId : string, firstName : string, lastName : string,  latestObservation : string, phoneNumber : string, phoneNumber : string, createdDt : string, email : string, closestDistance : number, }[]} pilots
     */
    function drawMap(pilots) {
        const canvas = document.getElementById("map");
        const context = canvas.getContext("2d");
        const size = Math.min(canvas.parentElement.offsetWidth, canvas.parentElement.offsetHeight) / 2;
        canvas.height = size;
        canvas.width = size;
        canvas.style.width = size +'px';
        canvas.style.height = size +'px';
        const padding = 10;
        const centerPos = {x: canvas.width / 2, y: canvas.height / 2};
        const radius = size / 2 - padding;

        context.fillStyle = '#FFFFFF';
        context.fillRect(0, 0, canvas.width, canvas.height);
        context.beginPath();
        context.arc(centerPos.x, centerPos.y, radius, 0, 2 * Math.PI, false);
        context.strokeStyle = '#000000';
        context.stroke();

        pilots.forEach((drone, index) => {
            if (drone == null) {
                return;
            }
            let pos = positionToCanvasPosition(drone.px, drone.py, radius, centerPos);
            context.beginPath();
            context.arc(pos.x, pos.y, 10, 0, 2 * Math.PI, false);
            context.strokeStyle = '#FF0000';
            context.fillStyle = '#FF0000';
            context.fill();
            context.beginPath();
            context.font = "14px Georgia";
            context.fontWeight = "bold";
            context.fillStyle = "black";
            context.fillText(index + 1, pos.x - 5, pos.y);
            context.fill();
        });
    }

    function positionToCanvasPosition(x, y, radius, center) {
        return {x: map(x, 150000, 350000, center.x-radius, center.x+radius), y :map(y, 150000, 350000, center.y-radius, center.y+radius)};
    }

    function map(value, x1, y1, x2, y2) {
        return (value - x1) * (y2 - x2) / (y1 - x1) + x2;
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
