import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { ProtectedRoute, PublicOnlyRoute } from './components/ProtectedRoute';
import { AuthProvider } from './context/AuthContext';
import { AboutPage } from './pages/AboutPage';
import { AdminConsultantDetailPage } from './pages/AdminConsultantDetailPage';
import { AdminConsultantsPage } from './pages/AdminConsultantsPage';
import { AdminStudentDetailPage } from './pages/AdminStudentDetailPage';
import { AdminStudentsPage } from './pages/AdminStudentsPage';
import { AssignedStudentsPage } from './pages/AssignedStudentsPage';
import { ConsultantStudentDetailPage } from './pages/ConsultantStudentDetailPage';
import { ContactPage } from './pages/ContactPage';
import { HealthCheckPage } from './pages/HealthCheckPage';
import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { NotFoundPage } from './pages/NotFoundPage';
import { RegisterPage } from './pages/RegisterPage';
import { ScholarshipsPage } from './pages/ScholarshipsPage';
import { ServicesPage } from './pages/ServicesPage';
import { StaffApplicationDetailPage } from './pages/StaffApplicationDetailPage';
import { StaffApplicationsPage } from './pages/StaffApplicationsPage';
import { StaffDashboardPage } from './pages/StaffDashboardPage';
import { StudentApplicationDetailPage } from './pages/StudentApplicationDetailPage';
import { StudentApplicationsPage } from './pages/StudentApplicationsPage';
import { StudentDashboardPage } from './pages/StudentDashboardPage';
import { StudentDocumentsPage } from './pages/StudentDocumentsPage';
import { StudentProfilePage } from './pages/StudentProfilePage';
import { StudyInUkPage } from './pages/StudyInUkPage';
import { UnauthorizedPage } from './pages/UnauthorizedPage';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Layout>
          <Routes>
            {/* Public routes */}
            <Route path="/" element={<LandingPage />} />
            <Route path="/about" element={<AboutPage />} />
            <Route path="/services" element={<ServicesPage />} />
            <Route path="/study-in-uk" element={<StudyInUkPage />} />
            <Route path="/scholarships" element={<ScholarshipsPage />} />
            <Route path="/contact" element={<ContactPage />} />
            <Route path="/health" element={<HealthCheckPage />} />

            {/* Auth routes */}
            <Route
              path="/login"
              element={
                <PublicOnlyRoute>
                  <LoginPage />
                </PublicOnlyRoute>
              }
            />
            <Route
              path="/register"
              element={
                <PublicOnlyRoute>
                  <RegisterPage />
                </PublicOnlyRoute>
              }
            />

            {/* Student routes */}
            <Route
              path="/student/dashboard"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <StudentDashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/student/profile"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <StudentProfilePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/student/applications"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <StudentApplicationsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/student/applications/:id"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <StudentApplicationDetailPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/student/documents"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <StudentDocumentsPage />
                </ProtectedRoute>
              }
            />

            {/* Shared Staff & Consultant routes */}
            <Route
              path="/staff/dashboard"
              element={
                <ProtectedRoute allowedRoles={['ADMIN', 'CONSULTANT']}>
                  <StaffDashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/staff/assigned-students"
              element={
                <ProtectedRoute allowedRoles={['CONSULTANT', 'ADMIN']}>
                  <AssignedStudentsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/staff/assigned-students/:id"
              element={
                <ProtectedRoute allowedRoles={['CONSULTANT', 'ADMIN']}>
                  <ConsultantStudentDetailPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/staff/applications"
              element={
                <ProtectedRoute allowedRoles={['CONSULTANT', 'ADMIN']}>
                  <StaffApplicationsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/staff/applications/:id"
              element={
                <ProtectedRoute allowedRoles={['CONSULTANT', 'ADMIN']}>
                  <StaffApplicationDetailPage />
                </ProtectedRoute>
              }
            />

            {/* Admin-only management routes */}
            <Route
              path="/staff/students"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminStudentsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/staff/students/:id"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminStudentDetailPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/staff/consultants"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminConsultantsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/staff/consultants/:id"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminConsultantDetailPage />
                </ProtectedRoute>
              }
            />

            <Route path="/unauthorized" element={<UnauthorizedPage />} />
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </Layout>
      </AuthProvider>
    </BrowserRouter>
  );
}
