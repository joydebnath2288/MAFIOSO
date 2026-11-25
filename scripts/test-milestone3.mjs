import pkg from '../e2e/node_modules/playwright/index.js';
const { chromium } = pkg;

async function runTests() {
  console.log('=====================================================');
  console.log('  MAFIOSO Milestone 3 Comprehensive Test Suite');
  console.log('=====================================================\n');

  const browser = await chromium.launch({ channel: 'chrome', headless: true });
  const context = await browser.newContext();
  const page = await context.newPage();

  let passed = 0;
  let failed = 0;

  function assert(condition, message) {
    if (condition) {
      console.log(`[PASS] ${message}`);
      passed++;
    } else {
      console.error(`[FAIL] ${message}`);
      failed++;
    }
  }

  try {
    // -------------------------------------------------------------------------
    // TEST 1 & 2: Public Pages & Navigation Links
    // -------------------------------------------------------------------------
    console.log('--- 1. Testing Public Pages & Navigation ---');

    // Home
    await page.goto('http://localhost/', { waitUntil: 'networkidle' });
    const homeTitle = await page.textContent('.hero-title');
    assert(homeTitle && homeTitle.includes('Your UK Study Journey Starts Here.'), `Home hero title matches: "${homeTitle}"`);

    // About
    await page.goto('http://localhost/about', { waitUntil: 'networkidle' });
    const aboutTitle = await page.textContent('.section-heading');
    assert(aboutTitle && aboutTitle.includes('Dedicated UK Education Advisory'), `About page heading matches: "${aboutTitle}"`);

    // Services
    await page.goto('http://localhost/services', { waitUntil: 'networkidle' });
    const serviceCards = await page.$$('.service-card');
    assert(serviceCards.length === 10, `All 10 service cards rendered: count=${serviceCards.length}`);

    // Study in UK
    await page.goto('http://localhost/study-in-uk', { waitUntil: 'networkidle' });
    const studyTitle = await page.textContent('.section-heading');
    assert(studyTitle && studyTitle.includes('Studying in the United Kingdom'), `Study in UK heading: "${studyTitle}"`);

    // Scholarships
    await page.goto('http://localhost/scholarships', { waitUntil: 'networkidle' });
    const scholarshipNotice = await page.textContent('.notice-banner-info');
    assert(scholarshipNotice && scholarshipNotice.includes('searchable university and scholarship directory will be launched in an upcoming milestone'), 'Scholarships page has clear future directory disclaimer');

    // Contact
    await page.goto('http://localhost/contact', { waitUntil: 'networkidle' });
    const contactTitle = await page.textContent('.section-heading');
    assert(contactTitle && contactTitle.includes('Contact & Student Inquiries'), `Contact heading: "${contactTitle}"`);

    // Inquiry submission
    await page.fill('#contact-fullname', 'Sadia Rahman');
    await page.fill('#contact-email', 'sadia.test@example.com');
    await page.fill('#contact-inquiry', 'Inquiring about BSc Engineering programs in London.');
    await page.click('button[type="submit"]');
    await page.waitForTimeout(400);
    const recordedBanner = await page.textContent('.notice-banner-info');
    assert(recordedBanner && recordedBanner.includes('Inquiry Recorded Locally'), 'Contact inquiry processed with honest local guidance banner');

    // -------------------------------------------------------------------------
    // TEST 3 & 4: Student Registration & Dashboard
    // -------------------------------------------------------------------------
    console.log('\n--- 2. Testing Student Registration & Dashboard ---');
    const timestamp = Date.now();
    const studentEmail = `student.${timestamp}@example.com`;
    const studentPassword = 'StudentPass#2026';
    const studentName = `Student User ${timestamp}`;

    await page.goto('http://localhost/register', { waitUntil: 'networkidle' });
    await page.fill('#reg-full-name', studentName);
    await page.fill('#reg-email', studentEmail);
    await page.fill('#reg-phone', '+8801711998877');
    await page.fill('#reg-password', studentPassword);
    await page.fill('#reg-confirm-password', studentPassword);
    await page.click('button[type="submit"]');

    await page.waitForTimeout(1000);
    const regSuccess = await page.textContent('.notice-banner-success');
    assert(regSuccess && regSuccess.includes('Student account created'), `Registration created account for: ${studentEmail}`);

    // Login as student
    await page.goto('http://localhost/login', { waitUntil: 'networkidle' });
    await page.fill('#login-email', studentEmail);
    await page.fill('#login-password', studentPassword);
    await page.click('button[type="submit"]');

    await page.waitForURL('**/student/dashboard', { timeout: 10000 });
    assert(page.url().includes('/student/dashboard'), `Student redirected to /student/dashboard: ${page.url()}`);

    // Verify student dashboard authentic info
    const studentWelcome = await page.textContent('.page-title');
    assert(studentWelcome && studentWelcome.includes(studentName), `Student dashboard displays student's actual name: "${studentWelcome}"`);

    const studentRoleBadge = await page.textContent('.mono-badge');
    assert(studentRoleBadge && studentRoleBadge.includes('STUDENT'), 'Role authority confirmed as STUDENT');

    // Verify empty states
    const emptyTitles = await page.$$eval('.empty-state-title', els => els.map(e => e.textContent.trim()));
    assert(
      emptyTitles.includes('No Applications Submitted') &&
      emptyTitles.includes('No Consultant Assigned Yet'),
      `Honest empty states present: ${emptyTitles.join(', ')}`
    );

    // Profile incomplete banner
    const profileIncomplete = await page.textContent('.section-card');
    assert(profileIncomplete && profileIncomplete.includes('Academic Profile Incomplete'), 'Incomplete profile banner displayed honestly');

    // Navigate to student profile
    await page.goto('http://localhost/student/profile', { waitUntil: 'networkidle' });
    assert(page.url().includes('/student/profile'), 'Student profile page accessible');

    // -------------------------------------------------------------------------
    // TEST 8: Student Cannot Access Staff Routes
    // -------------------------------------------------------------------------
    console.log('\n--- 3. Testing Student Route Security Boundaries ---');
    await page.goto('http://localhost/staff/dashboard', { waitUntil: 'networkidle' });
    assert(page.url().includes('/unauthorized') || !page.url().includes('/staff/dashboard'), `Student blocked from /staff/dashboard -> ${page.url()}`);

    await page.goto('http://localhost/staff/students', { waitUntil: 'networkidle' });
    assert(page.url().includes('/unauthorized') || !page.url().includes('/staff/students'), `Student blocked from /staff/students -> ${page.url()}`);

    // Sign out student
    await page.goto('http://localhost/student/dashboard', { waitUntil: 'networkidle' });
    const studentSignOutBtn = await page.$('button:has-text("Sign out")');
    if (studentSignOutBtn) {
      await studentSignOutBtn.click();
      await page.waitForURL('**/login', { timeout: 5000 });
      assert(page.url().includes('/login'), 'Student signed out successfully');
    }

    // -------------------------------------------------------------------------
    // TEST 5 & 7 & 9: Consultant Login & Permissions
    // -------------------------------------------------------------------------
    console.log('\n--- 4. Testing Consultant Login & Access ---');
    const consultantEmail = 'e2e.consultant.1795604315312.4815ef@example.com';
    const consultantPass = 'MafiosoE2E#2026';

    await page.goto('http://localhost/login', { waitUntil: 'networkidle' });
    await page.fill('#login-email', consultantEmail);
    await page.fill('#login-password', consultantPass);
    await page.click('button[type="submit"]');

    await page.waitForURL('**/staff/dashboard', { timeout: 10000 });
    assert(page.url().includes('/staff/dashboard'), `Consultant redirected to /staff/dashboard: ${page.url()}`);

    const consultantRole = await page.textContent('.mono-badge');
    assert(consultantRole && consultantRole.includes('CONSULTANT'), 'Consultant role badge verified');

    // Verify consultant does NOT see admin metrics
    const adminMetrics = await page.$('.stat-card-row');
    assert(adminMetrics === null, 'Admin platform metrics hidden from consultant');

    // Consultant can access assigned students
    await page.goto('http://localhost/staff/assigned-students', { waitUntil: 'networkidle' });
    const assignedCaseload = await page.textContent('.section-card');
    assert(assignedCaseload && assignedCaseload.includes('Student-to-consultant assignment relationships'), 'Assigned students displays honest empty state');

    // Consultant CANNOT access admin-only pages
    await page.goto('http://localhost/staff/students', { waitUntil: 'networkidle' });
    assert(page.url().includes('/unauthorized'), `Consultant blocked from /staff/students -> ${page.url()}`);

    // Sign out consultant
    await page.goto('http://localhost/staff/dashboard', { waitUntil: 'networkidle' });
    const consultantSignOut = await page.$('button:has-text("Sign out")');
    if (consultantSignOut) {
      await consultantSignOut.click();
      await page.waitForURL('**/login', { timeout: 5000 });
      assert(page.url().includes('/login'), 'Consultant signed out successfully');
    }

    // -------------------------------------------------------------------------
    // TEST 6 & 10 & 11: Admin Login & Management
    // -------------------------------------------------------------------------
    console.log('\n--- 5. Testing Admin Login & Management ---');
    const adminEmail = 'admin@gmail.com';
    const adminPass = 'Admin123#';

    await page.goto('http://localhost/login', { waitUntil: 'networkidle' });
    await page.fill('#login-email', adminEmail);
    await page.fill('#login-password', adminPass);
    await page.click('button[type="submit"]');

    await page.waitForURL('**/staff/dashboard', { timeout: 10000 });
    assert(page.url().includes('/staff/dashboard'), `Admin redirected to /staff/dashboard: ${page.url()}`);

    const adminRoleBadge = await page.textContent('.mono-badge');
    assert(adminRoleBadge && adminRoleBadge.includes('ADMIN'), 'Admin role authority verified');

    // Admin DOES see platform statistics
    await page.waitForSelector('.stat-card-row', { timeout: 10000 });
    const adminStatRow = await page.$('.stat-card-row');
    assert(adminStatRow !== null, 'Admin platform metric stat cards visible');

    // Admin can access student management
    await page.goto('http://localhost/staff/students', { waitUntil: 'networkidle' });
    const studentTable = await page.$('.data-table');
    assert(studentTable !== null, 'Admin can view and manage students table');

    // Admin can access consultant management
    await page.goto('http://localhost/staff/consultants', { waitUntil: 'networkidle' });
    const consultantTable = await page.$('.data-table');
    assert(consultantTable !== null, 'Admin can view and manage consultants table');

    // Sign out admin
    await page.goto('http://localhost/staff/dashboard', { waitUntil: 'networkidle' });
    const adminSignOut = await page.$('button:has-text("Sign out")');
    if (adminSignOut) {
      await adminSignOut.click();
      await page.waitForURL('**/login', { timeout: 5000 });
      assert(page.url().includes('/login'), 'Admin signed out successfully');
    }

    // -------------------------------------------------------------------------
    // TEST 15: Responsive Mobile Viewport
    // -------------------------------------------------------------------------
    console.log('\n--- 6. Testing Responsive Mobile Layout ---');
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto('http://localhost/', { waitUntil: 'networkidle' });

    const mobileBtn = await page.$('.mobile-menu-btn');
    assert(mobileBtn !== null, 'Mobile hamburger menu toggle visible on 375px viewport');

    if (mobileBtn) {
      await mobileBtn.click();
      await page.waitForTimeout(300);
      const drawer = await page.$('.mobile-drawer');
      assert(drawer !== null, 'Mobile navigation drawer opened');

      const servicesLink = await page.$('.mobile-drawer a[href="/services"]');
      if (servicesLink) {
        await servicesLink.click();
        await page.waitForURL('**/services', { timeout: 5000 });
        assert(page.url().includes('/services'), 'Mobile drawer navigated to /services on mobile');
      }
    }

  } catch (err) {
    console.error('Test encountered an error:', err);
    failed++;
  } finally {
    await browser.close();
  }

  console.log('\n=====================================================');
  console.log(`  RESULTS: ${passed} PASSED, ${failed} FAILED`);
  console.log('=====================================================');

  if (failed > 0) {
    process.exit(1);
  }
}

runTests();
