import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { ProtectedRoute, PublicOnlyRoute } from './components/ProtectedRoute';
import { AuthProvider } from './context/AuthContext';
import { AdminConsultantDetailPage } from './pages/AdminConsultantDetailPage';
import { AdminConsultantsPage } from './pages/AdminConsultantsPage';
import { AdminStudentDetailPage } from './pages/AdminStudentDetailPage';
import { AdminStudentsPage } from './pages/AdminStudentsPage';
import { AssignedStudentsPage } from './pages/AssignedStudentsPage';
import { HealthCheckPage } from './pages/HealthCheckPage';
import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { NotFoundPage } from './pages/NotFoundPage';
import { RegisterPage } from './pages/RegisterPage';
import { StaffDashboardPage } from './pages/StaffDashboardPage';
import { StudentDashboardPage } from './pages/StudentDashboardPage';
import { StudentProfilePage } from './pages/StudentProfilePage';
import { UnauthorizedPage } from './pages/UnauthorizedPage';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Layout>
          <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/health" element={<HealthCheckPage />} />
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
