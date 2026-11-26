import pkg from '../e2e/node_modules/playwright/index.js';
const { chromium } = pkg;

const BASE_URL = 'http://localhost';

async function apiSession(email, password) {
  const cookieJar = new Map();

  function updateCookies(headers) {
    const setCookies = headers.getSetCookie ? headers.getSetCookie() : [];
    for (const sc of setCookies) {
      const pair = sc.split(';')[0];
      const eq = pair.indexOf('=');
      if (eq > 0) {
        const name = pair.slice(0, eq).trim();
        const val = pair.slice(eq + 1).trim();
        if (val) {
          cookieJar.set(name, val);
        } else {
          cookieJar.delete(name);
        }
      }
    }
  }

  function cookieHeader() {
    return Array.from(cookieJar.entries())
      .map(([k, v]) => `${k}=${v}`)
      .join('; ');
  }

  let csrfToken = null;

  async function req(path, options = {}) {
    const method = (options.method || 'GET').toUpperCase();
    const headers = { ...(options.headers || {}) };
    const cookie = cookieHeader();
    if (cookie) {
      headers['Cookie'] = cookie;
    }
    if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method) && csrfToken) {
      headers['X-CSRF-Token'] = csrfToken;
    }
    let body = options.body;
    if (body && typeof body === 'object' && !(body instanceof FormData)) {
      headers['Content-Type'] = 'application/json';
      body = JSON.stringify(body);
    }
    const resp = await fetch(`${BASE_URL}${path}`, {
      method,
      headers,
      body,
    });
    updateCookies(resp.headers);
    const ct = resp.headers.get('content-type') || '';
    const json = ct.includes('application/json') ? await resp.json() : null;
    if (json && json.data && json.data.csrfToken) {
      csrfToken = json.data.csrfToken;
    }
    return { status: resp.status, ok: resp.ok, headers: resp.headers, json, resp };
  }

  const csrfRes = await req('/api/v1/auth/csrf');
  if (!csrfRes.ok) {
    throw new Error(`Failed to get CSRF token: ${csrfRes.status}`);
  }
  const loginRes = await req('/api/v1/auth/login', {
    method: 'POST',
    body: { email, password },
  });
  if (!loginRes.ok) {
    throw new Error(`Failed login for ${email}: ${loginRes.status} ${JSON.stringify(loginRes.json)}`);
  }

  return { req, user: loginRes.json.data.user };
}

async function registerStudentViaApi(fullName, email, password) {
  const tempJar = new Map();
  const csrfResp = await fetch(`${BASE_URL}/api/v1/auth/csrf`);
  for (const sc of csrfResp.headers.getSetCookie()) {
    const [k, v] = sc.split(';')[0].split('=');
    tempJar.set(k.trim(), v.trim());
  }
  const csrfJson = await csrfResp.json();
  const csrf = csrfJson.data.csrfToken;
  const regResp = await fetch(`${BASE_URL}/api/v1/auth/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Cookie: Array.from(tempJar.entries()).map(([k, v]) => `${k}=${v}`).join('; '),
      'X-CSRF-Token': csrf,
    },
    body: JSON.stringify({
      fullName,
      email,
      password,
      confirmPassword: password,
      phoneNumber: '+8801711223344',
    }),
  });
  if (!regResp.ok) {
    throw new Error(`Student register failed: ${regResp.status}`);
  }
  return (await regResp.json()).data.user;
}

async function runTests() {
  console.log('=====================================================');
  console.log('  MAFIOSO Milestone 4 Integration & Security Suite');
  console.log('=====================================================\n');

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

  const ts = Date.now();
  const studentAEmail = `m4.studentA.${ts}@example.com`;
  const studentBEmail = `m4.studentB.${ts}@example.com`;
  const studentPass = 'StudentPass#2026';

  const adminEmail = 'admin@gmail.com';
  const adminPass = 'Admin123#';

  const consultant1Email = `m4.consultant1.${ts}@example.com`;
  const consultant2Email = `m4.consultant2.${ts}@example.com`;
  const consultantPass = 'ConsultPass#2026';

  let appAId;
  let appARef;
  let docAId;

  try {
    // -------------------------------------------------------------------------
    // 1. Setup Accounts via API (Students + 2 Consultants created by Admin)
    // -------------------------------------------------------------------------
    console.log('--- 1. Provisioning Test Accounts ---');
    await registerStudentViaApi(`M4 Student A ${ts}`, studentAEmail, studentPass);
    await registerStudentViaApi(`M4 Student B ${ts}`, studentBEmail, studentPass);

    const admin = await apiSession(adminEmail, adminPass);
    const c1Create = await admin.req('/api/v1/admin/consultants', {
      method: 'POST',
      body: {
        fullName: `M4 Consultant One ${ts}`,
        email: consultant1Email,
        phoneNumber: '+447700900111',
        password: consultantPass,
        confirmPassword: consultantPass,
      },
    });
    assert(c1Create.status === 201, `Admin created consultant 1 (${consultant1Email})`);
    const consultant1Id = c1Create.json.data.id;

    const c2Create = await admin.req('/api/v1/admin/consultants', {
      method: 'POST',
      body: {
        fullName: `M4 Consultant Two ${ts}`,
        email: consultant2Email,
        phoneNumber: '+447700900222',
        password: consultantPass,
        confirmPassword: consultantPass,
      },
    });
    assert(c2Create.status === 201, `Admin created consultant 2 (${consultant2Email})`);
    const consultant2Id = c2Create.json.data.id;

    const studentA = await apiSession(studentAEmail, studentPass);
    const studentB = await apiSession(studentBEmail, studentPass);
    const consultant1 = await apiSession(consultant1Email, consultantPass);
    const consultant2 = await apiSession(consultant2Email, consultantPass);

    // -------------------------------------------------------------------------
    // 2. Student Profile Creation & Updates + Validation
    // -------------------------------------------------------------------------
    console.log('\n--- 2. Student Profile Management ---');
    const getProf0 = await studentA.req('/api/v1/student/profile');
    assert(getProf0.status === 200 && getProf0.json.data.academicProfileAvailable === true, 'GET /api/v1/student/profile succeeds');

    const badProf = await studentA.req('/api/v1/student/profile', {
      method: 'PUT',
      body: {
        fullName: 'A',
        passportIssueDate: '2030-01-01',
        passportExpiryDate: '2025-01-01',
        estimatedBudget: '-50',
      },
    });
    assert(badProf.status === 400 && badProf.json.fieldErrors, 'Backend rejects invalid profile fields with 400');

    const putProf = await studentA.req('/api/v1/student/profile', {
      method: 'PUT',
      body: {
        fullName: `Ayesha M4 ${ts}`,
        phoneNumber: '+8801711554433',
        dateOfBirth: '2001-08-20',
        gender: 'Female',
        nationality: 'Bangladeshi',
        presentAddress: 'Banani, Dhaka',
        permanentAddress: 'Sylhet, Bangladesh',
        passportNumber: 'BD4455667',
        passportIssueDate: '2022-03-01',
        passportExpiryDate: '2032-02-28',
        passportIssuingCountry: 'Bangladesh',
        highestQualification: 'Bachelor of Science',
        sscResult: 'GPA 5.00',
        hscResult: 'GPA 5.00',
        bachelorInstitution: 'BUET',
        bachelorSubject: 'Computer Science',
        bachelorCgpa: '3.78',
        bachelorGraduationYear: 2024,
        ieltsScore: '7.5',
        preferredDegreeLevel: 'MSc',
        preferredSubject: 'Artificial Intelligence',
        preferredUniversity: 'University of Edinburgh',
        preferredIntake: 'September 2026',
        preferredStudyLocation: 'Edinburgh',
        estimatedBudget: '28000',
        budgetCurrency: 'GBP',
        fundingSource: 'Family Sponsor',
        sponsorInfo: 'Father - Civil Engineer',
      },
    });
    assert(putProf.status === 200 && putProf.json.data.profile.completionPercent === 100, 'PUT /api/v1/student/profile updates all fields to 100% completion');

    // -------------------------------------------------------------------------
    // 3. Application Creation, Unique Reference, & Ownership Restrictions
    // -------------------------------------------------------------------------
    console.log('\n--- 3. Application Submission & Ownership Controls ---');
    const createApp = await studentA.req('/api/v1/student/applications', {
      method: 'POST',
      body: {
        degreeLevel: 'MSc',
        preferredSubject: 'Artificial Intelligence',
        intendedIntake: 'September 2026',
        preferredUniversity: 'University of Edinburgh',
        preferredLocation: 'Edinburgh',
        personalStatement: 'Focused on ML systems.',
      },
    });
    assert(createApp.status === 201 && /^MAF-\d{4}-\d{6}$/.test(createApp.json.data.referenceNumber), `Created application with unique reference: ${createApp.json?.data?.referenceNumber}`);
    appAId = createApp.json.data.id;
    appARef = createApp.json.data.referenceNumber;

    const createAppB = await studentB.req('/api/v1/student/applications', {
      method: 'POST',
      body: {
        degreeLevel: 'BSc',
        preferredSubject: 'Business Management',
        intendedIntake: 'January 2027',
        preferredUniversity: 'University of Leeds',
      },
    });
    assert(createAppB.status === 201 && createAppB.json.data.referenceNumber !== appARef, `Second application has distinct reference: ${createAppB.json?.data?.referenceNumber}`);

    // Ownership isolation: Student B cannot view Student A's application
    const stealApp = await studentB.req(`/api/v1/student/applications/${appAId}`);
    assert(stealApp.status === 403, `Student B blocked from viewing Student A application (HTTP ${stealApp.status})`);

    // Student A cannot mutate admin status
    const studentStatusHack = await studentA.req(`/api/v1/admin/applications/${appAId}/status`, {
      method: 'PUT',
      body: { status: 'COMPLETED' },
    });
    assert(studentStatusHack.status === 403, `Student blocked from admin status endpoint (HTTP ${studentStatusHack.status})`);

    // -------------------------------------------------------------------------
    // 4. Document Upload (PDF + Reject Executable), Download, & Ownership
    // -------------------------------------------------------------------------
    console.log('\n--- 4. Document Upload Security & Ownership ---');
    const badExeForm = new FormData();
    badExeForm.append('category', 'PASSPORT');
    badExeForm.append('file', new Blob([new Uint8Array([0x4d, 0x5a, 0x90, 0x00, 0x03])], { type: 'application/pdf' }), 'spoofed.pdf');
    const badUpload = await studentA.req(`/api/v1/student/applications/${appAId}/documents`, {
      method: 'POST',
      body: badExeForm,
    });
    assert(badUpload.status === 400, `Backend rejected MZ executable disguised as PDF (HTTP ${badUpload.status})`);

    const validPdfBytes = new TextEncoder().encode('%PDF-1.4\n1 0 obj\n<<>>\nendobj\n%%EOF\n');
    const goodForm = new FormData();
    goodForm.append('category', 'PASSPORT');
    goodForm.append('file', new Blob([validPdfBytes], { type: 'application/pdf' }), 'ayesha_passport.pdf');
    const goodUpload = await studentA.req(`/api/v1/student/applications/${appAId}/documents`, {
      method: 'POST',
      body: goodForm,
    });
    assert(goodUpload.status === 201 && goodUpload.json.data.reviewStatus === 'PENDING', `Uploaded valid PDF document #${goodUpload.json?.data?.id}`);
    docAId = goodUpload.json.data.id;

    // Student B cannot download Student A's document
    const stealDoc = await studentB.req(`/api/v1/documents/${docAId}/download`);
    assert(stealDoc.status === 403, `Student B blocked from downloading Student A document (HTTP ${stealDoc.status})`);

    // Student A can download own document
    const ownDownload = await studentA.req(`/api/v1/documents/${docAId}/download`);
    const downloadedBuf = new Uint8Array(await ownDownload.resp.arrayBuffer());
    assert(ownDownload.status === 200 && downloadedBuf.length === validPdfBytes.length, `Student A downloaded own document (${downloadedBuf.length} bytes)`);

    // -------------------------------------------------------------------------
    // 5. Admin Consultant Assignment, Reassignment, & Access Restrictions
    // -------------------------------------------------------------------------
    console.log('\n--- 5. Consultant Assignment & Access Restrictions ---');
    // Before assignment, neither consultant can access appA or docA
    const unauthC1 = await consultant1.req(`/api/v1/consultant/applications/${appAId}`);
    assert(unauthC1.status === 403, `Unassigned consultant blocked from application (HTTP ${unauthC1.status})`);

    const assign1 = await admin.req(`/api/v1/admin/applications/${appAId}/assign-consultant`, {
      method: 'POST',
      body: { consultantId: consultant1Id, note: 'Initial MSc AI pairing.' },
    });
    assert(assign1.status === 200 && assign1.json.data.application.status === 'ASSIGNED', 'Admin assigned consultant 1 -> application moved to ASSIGNED');

    // Consultant 1 can now view application, student, and download document
    const authC1App = await consultant1.req(`/api/v1/consultant/applications/${appAId}`);
    assert(authC1App.status === 200, 'Assigned consultant 1 can view application detail');

    const authC1Stu = await consultant1.req(`/api/v1/consultant/assigned-students/${studentA.user.id}`);
    assert(authC1Stu.status === 200 && authC1Stu.json.data.profile.passportNumber === 'BD4455667', 'Assigned consultant 1 can view student profile');

    const authC1Doc = await consultant1.req(`/api/v1/documents/${docAId}/download`);
    assert(authC1Doc.status === 200, 'Assigned consultant 1 can download student document');

    // Consultant 2 is STILL blocked
    const blockedC2App = await consultant2.req(`/api/v1/consultant/applications/${appAId}`);
    assert(blockedC2App.status === 403, `Other consultant 2 blocked from consultant 1 application (HTTP ${blockedC2App.status})`);
    const blockedC2Stu = await consultant2.req(`/api/v1/consultant/assigned-students/${studentA.user.id}`);
    assert(blockedC2Stu.status === 403, `Other consultant 2 blocked from unassigned student profile (HTTP ${blockedC2Stu.status})`);
    const blockedC2Doc = await consultant2.req(`/api/v1/documents/${docAId}/download`);
    assert(blockedC2Doc.status === 403, `Other consultant 2 blocked from downloading unassigned document (HTTP ${blockedC2Doc.status})`);

    // Consultant 1 cannot set CANCELLED
    const badC1Status = await consultant1.req(`/api/v1/consultant/applications/${appAId}/status`, {
      method: 'PUT',
      body: { status: 'CANCELLED', note: 'Trying admin status' },
    });
    assert(badC1Status.status === 400, `Consultant blocked from setting CANCELLED (HTTP ${badC1Status.status})`);

    // Consultant 1 updates status to IN_PROGRESS
    const goodC1Status = await consultant1.req(`/api/v1/consultant/applications/${appAId}/status`, {
      method: 'PUT',
      body: { status: 'IN_PROGRESS', note: 'Verified academic prerequisites.' },
    });
    assert(goodC1Status.status === 200 && goodC1Status.json.data.statusHistory.length === 3, 'Consultant updated status -> IN_PROGRESS with 3 history entries');

    // Document rejection requires note; then approve
    const rejectNoNote = await consultant1.req(`/api/v1/consultant/documents/${docAId}/review`, {
      method: 'PUT',
      body: { reviewStatus: 'REJECTED', reviewNote: '' },
    });
    assert(rejectNoNote.status === 400, 'Rejecting document without reviewNote rejected with 400');

    const rejectWithNote = await consultant1.req(`/api/v1/consultant/documents/${docAId}/review`, {
      method: 'PUT',
      body: { reviewStatus: 'REJECTED', reviewNote: 'Please upload color scan showing full MRZ.' },
    });
    assert(rejectWithNote.status === 200 && rejectWithNote.json.data.reviewStatus === 'REJECTED', 'Document rejected with correction note');

    const approveDoc = await admin.req(`/api/v1/admin/documents/${docAId}/review`, {
      method: 'PUT',
      body: { reviewStatus: 'APPROVED', reviewNote: 'Verified by admin audit.' },
    });
    assert(approveDoc.status === 200 && approveDoc.json.data.reviewStatus === 'APPROVED', 'Admin approved document');

    // Reassign to consultant 2, then remove assignment
    const reassign = await admin.req(`/api/v1/admin/applications/${appAId}/assign-consultant`, {
      method: 'POST',
      body: { consultantId: consultant2Id, note: 'Reassigned to specialist.' },
    });
    assert(reassign.status === 200 && reassign.json.data.application.assignedConsultant.id === consultant2Id, 'Admin reassigned application to consultant 2');

    const oldC1Blocked = await consultant1.req(`/api/v1/consultant/applications/${appAId}`);
    assert(oldC1Blocked.status === 403, `Previous consultant 1 immediately loses access after reassignment (HTTP ${oldC1Blocked.status})`);

    // Remove assignment and verify consultant 2 loses access
    const removeAssign = await admin.req(`/api/v1/admin/applications/${appAId}/assign-consultant?note=Testing+removal`, {
      method: 'DELETE',
    });
    assert(removeAssign.status === 200 && !removeAssign.json.data.application.assignedConsultant, 'Admin removed consultant assignment');
    const c2AfterRemove = await consultant2.req(`/api/v1/consultant/applications/${appAId}`);
    assert(c2AfterRemove.status === 403, `Removed consultant 2 loses access immediately (HTTP ${c2AfterRemove.status})`);

    // Re-assign consultant 2 so UI checks show assigned consultant
    const assignBack = await admin.req(`/api/v1/admin/applications/${appAId}/assign-consultant`, {
      method: 'POST',
      body: { consultantId: consultant2Id, note: 'Assigned for UI verification.' },
    });
    assert(assignBack.status === 200, 'Admin assigned consultant 2 for UI check');

    // -------------------------------------------------------------------------
    // 6. Browser UI End-to-End Verification (Playwright)
    // -------------------------------------------------------------------------
    console.log('\n--- 6. Playwright UI Verification ---');
    const browser = await chromium.launch({ channel: 'chrome', headless: true });
    const context = await browser.newContext();
    const page = await context.newPage();
    const pageErrors = [];
    page.on('pageerror', (err) => pageErrors.push(err.message));

    // Student logs in and sees real profile completion, real application, real assigned consultant
    await page.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle' });
    await page.fill('#login-email', studentAEmail);
    await page.fill('#login-password', studentPass);
    await page.click('button[type="submit"]');
    await page.waitForURL('**/student/dashboard', { timeout: 10000 });
    await page.waitForSelector('.profile-completion-bar-fill', { timeout: 10000 });

    const stuBody = await page.textContent('main');
    assert(stuBody.includes('100%') && stuBody.includes(appARef), `Student dashboard shows real 100% completion and ${appARef}`);

    await page.goto(`${BASE_URL}/student/profile`, { waitUntil: 'networkidle' });
    await page.waitForSelector('#prof-passport-num', { timeout: 10000 });
    const passportVal = await page.inputValue('#prof-passport-num');
    assert(passportVal === 'BD4455667', 'Student profile form renders persisted passport details');

    await page.goto(`${BASE_URL}/student/applications/${appAId}`, { waitUntil: 'networkidle' });
    await page.waitForSelector('.workflow-stepper', { timeout: 10000 });
    const appDetailText = await page.textContent('main');
    assert(appDetailText.includes(appARef) && appDetailText.includes('Verified academic prerequisites'), 'Student application detail renders timeline & status history');

    await page.goto(`${BASE_URL}/student/documents`, { waitUntil: 'networkidle' });
    await page.waitForSelector('.data-table', { timeout: 10000 });
    const docTableText = await page.textContent('main');
    assert(docTableText.includes('ayesha_passport.pdf') && docTableText.includes('APPROVED'), 'Student documents vault shows uploaded file and APPROVED status');

    await page.click('button:has-text("Sign out")');
    await page.waitForURL('**/login', { timeout: 5000 });

    // Consultant 2 logs in and sees assigned student & application
    await page.fill('#login-email', consultant2Email);
    await page.fill('#login-password', consultantPass);
    await page.click('button[type="submit"]');
    await page.waitForURL('**/staff/dashboard', { timeout: 10000 });
    await page.waitForSelector('.stat-card-row', { timeout: 10000 });
    const conDashText = await page.textContent('main');
    assert(conDashText.includes(appARef), `Consultant dashboard shows assigned application ${appARef}`);

    await page.goto(`${BASE_URL}/staff/assigned-students`, { waitUntil: 'networkidle' });
    await page.waitForSelector('.data-table', { timeout: 10000 });
    const conStuText = await page.textContent('main');
    assert(conStuText.includes(`Ayesha M4 ${ts}`), 'Consultant assigned students table displays student');

    await page.click('button:has-text("Sign out")');
    await page.waitForURL('**/login', { timeout: 5000 });

    // Admin logs in and sees Application Management
    await page.fill('#login-email', adminEmail);
    await page.fill('#login-password', adminPass);
    await page.click('button[type="submit"]');
    await page.waitForURL('**/staff/dashboard', { timeout: 10000 });

    await page.goto(`${BASE_URL}/staff/applications`, { waitUntil: 'networkidle' });
    await page.waitForSelector('.data-table', { timeout: 10000 });
    const adminAppsText = await page.textContent('main');
    assert(adminAppsText.includes(appARef), `Admin applications table lists ${appARef}`);

    assert(pageErrors.length === 0, `Zero uncaught frontend JS errors (${pageErrors.length})`);
    await browser.close();
  } catch (err) {
    console.error('Test error:', err);
    failed++;
  }

  console.log('\n=====================================================');
  console.log(`  RESULTS: ${passed} PASSED, ${failed} FAILED`);
  console.log('=====================================================');
  if (failed > 0) {
    process.exit(1);
  }
}

runTests();
