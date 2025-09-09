// Utility Functions
function formatCurrency(amount) {
    return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        minimumFractionDigits: 2
    }).format(amount);
}

function showMessage(elementId, message, isError = false) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = message;
        element.style.display = 'block';
        element.style.backgroundColor = isError ? '#fed7d7' : '#bee3f8';
        element.style.color = isError ? '#c53030' : '#1a365d';
        
        // Auto-hide after 5 seconds
        setTimeout(() => {
            element.style.display = 'none';
        }, 5000);
    }
}

// Phone number masking function
function maskPhoneNumber(phone) {
    if (!phone || phone.trim() === '' || phone === 'N/A') return 'N/A';

    // Keep only digits
    const digits = phone.replace(/\D/g, '');

    if (digits.length <= 4) {
        return digits;
    }

    // Show only last 4 digits with X's for the rest
    return 'XXXXXX' + digits.slice(-4);
}

// Calculator Functions
let currentInput = '';

function appendNumber(number) {
    currentInput += number;
    document.querySelector('input[name="number"]').value = currentInput;
}

function clearInput() {
    currentInput = '';
    document.querySelector('input[name="number"]').value = '';
    document.getElementById('result').textContent = '';
}

async function calculate(event) {
    event.preventDefault();

    const numberInput = document.querySelector('input[name="number"]');
    const number = parseFloat(numberInput.value);

    if (isNaN(number)) {
        showMessage('result', 'Please enter a valid number', true);
        return;
    }

    try {
        const response = await fetch('/api/calculate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `number=${encodeURIComponent(number)}`
        });

        if (response.ok) {
            const result = await response.text();
            document.getElementById('result').textContent = result;
            document.getElementById('result').style.display = 'block';
        } else {
            throw new Error('Calculation failed');
        }
    } catch (error) {
        showMessage('result', 'Error performing calculation', true);
        console.error('Error:', error);
    }
}

// Bill Generation Functions
async function generateBill(event) {
    event.preventDefault();

    const form = event.target;
    const formData = new FormData(form);
    const customerName = formData.get('customerName');
    const phoneNumber = formData.get('phoneNumber');
    const amount = parseFloat(formData.get('amount'));

    // Validate inputs (phone is now optional)
    if (!customerName || !amount) {
        showMessage('message', 'Customer name and amount are required', true);
        return;
    }

    if (phoneNumber && !/^\d{0,10}$/.test(phoneNumber)) {
        showMessage('message', 'Phone number must be up to 10 digits', true);
        return;
    }

    try {
        const params = new URLSearchParams({
            customerName,
            phoneNumber: phoneNumber || '',
            amount,
            sendSms: phoneNumber ? 'true' : 'false'
        });

        const response = await fetch('/api/bill/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: params
        });

        if (response.ok) {
            const billDetails = await response.text();
            document.getElementById('billDetails').textContent = billDetails;
            document.getElementById('billDetails').style.display = 'block';
            showMessage('message', 'Bill generated successfully!');

            // Clear form
            form.reset();
        } else {
            throw new Error('Failed to generate bill');
        }
    } catch (error) {
        showMessage('message', 'Error generating bill: ' + error.message, true);
        console.error('Error:', error);
    }
}

// Bills View Functions
function toggleCollapse(element) {
    element.classList.toggle('collapsed');
}

function filterBills() {
    const searchTerm = document.getElementById('searchName').value.toLowerCase();
    const customerNames = document.querySelectorAll('.customer-name');
    let found = false;

    customerNames.forEach(cell => {
        const row = cell.closest('tr');
        const customerName = cell.textContent.toLowerCase();

        if (customerName.includes(searchTerm)) {
            row.style.display = '';
            found = true;

            // Expand parent sections
            let parent = row.closest('.day-container');
            if (parent) {
                const dayHeader = parent.previousElementSibling;
                if (dayHeader && dayHeader.classList.contains('collapsed')) {
                    dayHeader.classList.remove('collapsed');
                }

                parent = parent.closest('.month-container');
                if (parent) {
                    const monthHeader = parent.previousElementSibling;
                    if (monthHeader && monthHeader.classList.contains('collapsed')) {
                        monthHeader.classList.remove('collapsed');
                    }

                    parent = parent.closest('.months-wrapper');
                    if (parent) {
                        const yearHeader = parent.previousElementSibling;
                        if (yearHeader && yearHeader.classList.contains('collapsed')) {
                            yearHeader.classList.remove('collapsed');
                        }
                    }
                }
            }
        } else {
            row.style.display = 'none';
        }
    });

    document.getElementById('no-results').style.display = found ? 'none' : 'block';
}

function resetSearch() {
    document.getElementById('searchName').value = '';
    document.querySelectorAll('tr').forEach(row => {
        row.style.display = '';
    });
    document.getElementById('no-results').style.display = 'none';
}

async function deleteBill(date, sequence) {
    if (!confirm('Are you sure you want to delete this bill?')) {
        return;
    }

    try {
        const response = await fetch('/api/bill/delete', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `date=${date}&dailySequence=${sequence}`
        });

        if (response.ok) {
            alert('Bill deleted successfully');
            location.reload();
        } else {
            const errorText = await response.text();
            throw new Error(errorText);
        }
    } catch (error) {
        alert('Error deleting bill: ' + error.message);
        console.error('Error:', error);
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Add event listeners for input validation
    const phoneInputs = document.querySelectorAll('input[type="tel"]');
    phoneInputs.forEach(input => {
        // Add (Optional) indicator
        input.placeholder = input.placeholder + ' (Optional)';

        input.addEventListener('input', function() {
            this.value = this.value.replace(/\D/g, '').substring(0, 10);
        });
    });

    const amountInputs = document.querySelectorAll('input[type="number"]');
    amountInputs.forEach(input => {
        input.addEventListener('input', function() {
            if (this.value < 0) this.value = 0;
        });

        input.addEventListener('keypress', function(e) {
            // Allow only numbers and decimal point
            if (e.key === '.' && this.value.includes('.')) {
                e.preventDefault();
            } else if (e.key !== '.' && isNaN(parseInt(e.key))) {
                e.preventDefault();
            }
        });
    });

    // Mask phone numbers in bills view
    if (document.getElementById('searchName')) {
        const phoneCells = document.querySelectorAll('td:nth-child(3)'); // Assuming phone is in 3rd column
        phoneCells.forEach(cell => {
            const originalPhone = cell.textContent.trim();
            cell.textContent = maskPhoneNumber(originalPhone);
            cell.classList.add('phone-masked');

            // Add title with full number for hover
            if (originalPhone && originalPhone !== 'N/A') {
                cell.title = 'Phone: ' + originalPhone;
            }
        });
    }

    // Initialize bills view if on that page
    if (document.getElementById('searchName')) {
        // Expand today's bills by default
        const today = new Date().toISOString().split('T')[0];
        const todayElements = document.querySelectorAll('.day-header');

        todayElements.forEach(element => {
            if (element.textContent.includes(today)) {
                const dayHeader = element.closest('.day-header');
                if (dayHeader && dayHeader.classList.contains('collapsed')) {
                    dayHeader.classList.remove('collapsed');
                }

                const monthHeader = dayHeader.closest('.month-header');
                if (monthHeader && monthHeader.classList.contains('collapsed')) {
                    monthHeader.classList.remove('collapsed');
                }

                const yearHeader = monthHeader.closest('.year-header');
                if (yearHeader && yearHeader.classList.contains('collapsed')) {
                    yearHeader.classList.remove('collapsed');
                }
            }
        });
    }
});

// Helper function for contains selector
if (!Element.prototype.matches) {
    Element.prototype.matches = 
        Element.prototype.matchesSelector || 
        Element.prototype.mozMatchesSelector ||
        Element.prototype.msMatchesSelector || 
        Element.prototype.oMatchesSelector || 
        Element.prototype.webkitMatchesSelector ||
        function(s) {
            var matches = (this.document || this.ownerDocument).querySelectorAll(s),
                i = matches.length;
            while (--i >= 0 && matches.item(i) !== this) {}
            return i > -1;            
        };
}
