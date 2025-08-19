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