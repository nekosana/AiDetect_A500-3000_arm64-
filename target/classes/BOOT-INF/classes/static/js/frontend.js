console.log("jsjsjsjs");
const button1 = document.getElementById("click-me-button");
button1.addEventListener("click", function() {
	console.log("Button 1 clicked!");
	handleClickMe("Button 1");
});

function handleClickMe(buttonName) {
	alert(buttonName + " clicked!");
}


const submitButton = document.getElementById("filter-button");
submitButton.addEventListener("click", function() {
	const input1Value = document.getElementById("lower-bound").value;
	const input2Value = document.getElementById("upper-bound").value;
	const id = document.getElementById("id").value;
	if (input1Value > input2Value) {
		alert('The lower bound cannot be greater than the upper bound.');
		return;
	}
	// if any of the three value is null, then return an alert
	if (input1Value === "" || input2Value === "" || id === "") {
		alert('Please fill in all the fields.');
		return;
	}

	const s = "{\n" +
		"  \"WHERE\": {\n" +
		"    \"AND\": [\n" +
		"      {\n" +
		"        \"GT\": {\n" +
		"          \"" + id + "_avg\": " + input1Value + "\n" +
		"        }\n" +
		"      },\n" +
		"      {\n" +
		"        \"LT\": {\n" +
		"          \"" + id + "_avg\": " + input2Value + "\n" +
		"        }\n" +
		"      }\n" +
		"    ]\n" +
		"  },\n" +
		"  \"OPTIONS\": {\n" +
		"    \"COLUMNS\": [\n" +
		"      \"" + id + "_dept\",\n" +
		"      \"" + id + "_uuid\",\n" +
		"      \"" + id + "_avg\",\n" +
		"      \"" + id + "_title\",\n" +
		"      \"" + id + "_year\",\n" +
		"      \"" + id + "_instructor\",\n" +
		"      \"" + id + "_pass\",\n" +
		"      \"" + id + "_fail\",\n" +
		"      \"" + id + "_audit\"\n" +
		"    ],\n" +
		"    \"ORDER\": \"" + id + "_avg\"\n" +
		"  }\n" +
		"}"
	alert(s);
});


const searchButton = document.getElementById('search-button');
const deptInput = document.getElementById('dept');
const uuidInput = document.getElementById('uuid');
const searchId = document.getElementById('search-id');

searchButton.addEventListener('click', async function(event) {
	event.preventDefault(); // prevent default button click behavior

	const deptValue = deptInput.value;
	const uuidValue = uuidInput.value;
	const id = searchId.value;

	const query = {
		WHERE: {
			AND: [
				{
					IS: {
						[id + "_dept"]: deptValue
					}
				},
				{
					IS: {
						[id + "_uuid"]: uuidValue
					}
				}
			]
		},
		OPTIONS: {
			COLUMNS: [
				id + "_dept",
				id + "_uuid",
				id + "_avg",
				id + "_title",
				id + "_year",
				id + "_instructor",
				id + "_pass",
				id + "_fail",
				id + "_audit"
			],
			ORDER: id + "_avg"
		}
	};
	console.log('Query:', query);

	try {
		const response = await fetch('http://localhost:8088/query', {
			method: 'POST',
			body: JSON.stringify(query),
			headers: {
				'Content-Type': 'application/json'
			}
		});

		if (response.ok) {
			const data = await response.json();
			console.log(data);

			if (data.result && data.result.length > 0) {
				// Create table and add it to the container
				const table = await createTable(data.result);
				const tableContainer = document.getElementById('search-table-container');
				tableContainer.innerHTML = ''; // Clear previous table if there's any
				tableContainer.appendChild(table);
			} else {
				alert('No data found for the provided query');
			}
		} else {
			const errorResponse = await response.json();
			alert('Error performing query: ' + errorResponse.error);
		}
	} catch (error) {
		alert('Error performing query: ' + error);
	}

	// Clear the form input fields
	deptInput.value = '';
	uuidInput.value = '';
});




const displayTableButton = document.getElementById("display-table-button");
const tableContainer = document.getElementById("table-container");

displayTableButton.addEventListener("click", function() {
	// Create table element
	const table = document.createElement("table");
	const headerRow = document.createElement("tr");

	// Create table headers
	const headers = ["Name", "Age", "City"];
	headers.forEach(header => {
		const th = document.createElement("th");
		th.textContent = header;
		headerRow.appendChild(th);
	});
	table.appendChild(headerRow);

	// Create table rows
	const data = [
		{ name: "John Doe", age: 30, city: "New York" },
		{ name: "Jane Smith", age: 25, city: "San Francisco" },
		{ name: "Bob Johnson", age: 40, city: "Chicago" }
	];
	data.forEach(rowData => {
		const row = document.createElement("tr");
		const nameCell = document.createElement("td");
		const ageCell = document.createElement("td");
		const cityCell = document.createElement("td");

		nameCell.textContent = rowData.name;
		ageCell.textContent = rowData.age;
		cityCell.textContent = rowData.city;

		row.appendChild(nameCell);
		row.appendChild(ageCell);
		row.appendChild(cityCell);
		table.appendChild(row);
	});

	// Add the table to the HTML page
	tableContainer.innerHTML = "";
	tableContainer.appendChild(table);
});


// fetch("http://localhost:4321/datasets")
// 	.then(response => response.json())
// 	.then(data => console.log(data))
// 	.catch(error => console.error(error));

const uploadForm = document.getElementById("upload-form");
uploadForm.addEventListener("submit", uploadDataset);

async function uploadDataset(event) {
	event.preventDefault(); // prevent default form submit behavior

	const datasetFileInput = document.getElementById("dataset-file");
	const datasetIdInput = document.getElementById("dataset-id");
	const datasetTypeInput = document.getElementById("dataset-type");

	const datasetFile = datasetFileInput.files[0];
	const datasetId = datasetIdInput.value;
	const datasetType = datasetTypeInput.value;

	// Read the file as an ArrayBuffer
	const fileReader = new FileReader();
	fileReader.onload = async function (event) {
		const fileArrayBuffer = event.target.result;

		try {
			const response = await fetch(`/dataset/${datasetId}/${datasetType}`, {
				method: "PUT",
				body: new Uint8Array(fileArrayBuffer),
				headers: {
					"Content-Type": "application/x-zip-compressed"
				}
			});

			if (response.ok) {
				alert("Dataset uploaded successfully!");
			} else {
				const errorResponse = await response.json();
				alert("Error uploading dataset: " + errorResponse.error);
			}
		} catch (error) {
			alert("Error uploading dataset: " + error);
		}
	};
	fileReader.readAsArrayBuffer(datasetFile);

	// Clear the form input fields
	datasetFileInput.value = "";
	datasetIdInput.value = "";
	datasetTypeInput.value = "";
}

async function createTable(data) {
	const table = document.createElement('table');
	const header = table.createTHead();
	const headerRow = header.insertRow();
	const columns = Object.keys(data[0]);

	// Create table header
	columns.forEach(column => {
		const headerCell = document.createElement('th');
		headerCell.textContent = column;
		headerRow.appendChild(headerCell);
	});

	// Create table body
	const tbody = table.createTBody();
	data.forEach(row => {
		const tableRow = tbody.insertRow();
		columns.forEach(column => {
			const cell = tableRow.insertCell();
			cell.textContent = row[column];
		});
	});

	return table;
}





