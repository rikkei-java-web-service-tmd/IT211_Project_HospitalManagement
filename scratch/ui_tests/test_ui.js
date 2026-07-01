const puppeteer = require('puppeteer');
const fs = require('fs');

(async () => {
    console.log("Starting E2E UI Test with Puppeteer...");
    const browser = await puppeteer.launch({ headless: "new" });
    const page = await browser.newPage();
    await page.setViewport({ width: 1280, height: 800 });

    try {
        console.log("Navigating to http://localhost:8080/ ...");
        await page.goto('http://localhost:8080/', { waitUntil: 'networkidle2' });
        await page.screenshot({ path: 'step1_login.png' });

        console.log("Clicking on Register Link...");
        await page.click('a[href="/register"]');
        await page.waitForNavigation({ waitUntil: 'networkidle2' });
        await page.screenshot({ path: 'step2_register.png' });

        const testUser = "ui_auto_" + Date.now();
        console.log("Registering new patient: " + testUser);
        await page.type('#username', testUser);
        await page.type('#password', 'pass123');
        
        // Setup dialog handler for the alert
        page.on('dialog', async dialog => {
            console.log("Alert popup: " + dialog.message());
            await dialog.accept();
        });

        await Promise.all([
            page.waitForNavigation({ waitUntil: 'networkidle2' }),
            page.click('button[type="submit"]')
        ]);
        console.log("Registered and redirected back to Login.");

        console.log("Logging in as " + testUser + " ...");
        await page.type('#username', testUser);
        await page.type('#password', 'pass123');
        
        await Promise.all([
            page.waitForNavigation({ waitUntil: 'networkidle2' }),
            page.click('button[type="submit"]')
        ]);
        
        console.log("Logged in. Checking Dashboard URL...");
        if (!page.url().includes('patient-dashboard')) {
            throw new Error("Failed to reach patient-dashboard, currently at: " + page.url());
        }
        await page.screenshot({ path: 'step3_dashboard_empty.png' });

        console.log("Booking an appointment...");
        // Click BOOK APPOINTMENT button
        await page.evaluate(() => {
            document.querySelector("button[onclick=\"document.getElementById('booking-form-container').style.display='block'\"]").click();
        });
        
        await page.waitForSelector('#apt-date', { visible: true });
        await page.type('#apt-date', '2026-10-10');
        await page.type('#apt-time', '09:00 - 09:30');
        await page.type('#apt-symptom', 'E2E Testing Headaches');
        
        // Wait for response and table update
        await page.click('#booking-form button[type="submit"]');
        
        // Wait a second for fetch to finish and table to reload
        await new Promise(resolve => setTimeout(resolve, 2000));
        await page.screenshot({ path: 'step4_dashboard_booked.png' });
        
        const tableContent = await page.$eval('#appointments-tbody', tbody => tbody.innerText);
        console.log("Appointments Table Content:\n" + tableContent);
        
        if (tableContent.includes('E2E Testing Headaches')) {
            console.log("SUCCESS! Appointment was booked and is visible on UI.");
        } else {
            console.error("FAIL! Appointment not found in table.");
        }

        console.log("UI Automation Test completed successfully!");
    } catch (e) {
        console.error("Test failed: ", e);
        await page.screenshot({ path: 'error_screenshot.png' });
    } finally {
        await browser.close();
    }
})();
