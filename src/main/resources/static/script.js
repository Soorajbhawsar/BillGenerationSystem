function appendNumber(num) {
    document.querySelector('input[name="number"]').value += num;
}

function clearInput() {
    document.querySelector('input[name="number"]').value = "";
}

async function calculate(e) {
    e.preventDefault();
    const inputField = document.querySelector('input[name="number"]');
    const number = inputField.value;

    try {
        const response = await fetch('/api/calculate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `number=${number}`
        });

        if (!response.ok) {
            throw new Error(await response.text());
        }

        const result = await response.text();
        document.getElementById("result").textContent = result;
        inputField.value = "";
    } catch (error) {
        document.getElementById("result").textContent = "Error: " + error.message;
    }
}

async function generateBill(e) {
    e.preventDefault();
    const form = e.target;
    const formData = new FormData(e.target);
    const params = new URLSearchParams(formData);

    try {
        const response = await fetch('/api/bill/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: params
        });

        if (!response.ok) {
            throw new Error(await response.text());
        }

        const billDetails = await response.text();
        document.getElementById("billDetails").textContent = billDetails;
        document.getElementById("message").textContent = "Bill generated successfully!";
        form.reset();
    } catch (error) {
        document.getElementById("message").textContent = "Error: " + error.message;
    }
}


    async function deleteBill(id) {
        if (confirm('Are you sure you want to delete this bill?')) {
            try {
                const response = await fetch(`/api/bill/delete/${id}`, {
                    method: 'POST'
                });

                if (!response.ok) {
                    throw new Error(await response.text());
                }

                alert('Bill deleted successfully');
                window.location.reload();
            } catch (error) {
                alert('Error deleting bill: ' + error.message);
            }
        }
    }

    function filterBills() {
        const searchTerm = document.getElementById('searchName').value.toLowerCase();
        const rows = document.querySelectorAll('#detailedBillsTable tbody tr');
        let found = false;

        rows.forEach(row => {
            const cell = row.querySelector('.customer-name');
            const customerName = cell.textContent.toLowerCase();
            if (customerName.includes(searchTerm)) {
                row.style.display = '';
                found = true;
            } else {
                row.style.display = 'none';
            }
        });

        document.getElementById('no-results').style.display = found || searchTerm === '' ? 'none' : 'block';
    }

    function resetSearch() {
        document.getElementById('searchName').value = '';
        const rows = document.querySelectorAll('#detailedBillsTable tbody tr');
        rows.forEach(row => {
            row.style.display = '';
        });
        document.getElementById('no-results').style.display = 'none';
    }
