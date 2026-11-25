import { Footer } from './Footer';
import { Navbar } from './Navbar';

export function Layout({ children }) {
  return (
    <div className="app-shell">
      <Navbar />
      <main className="app-main">{children}</main>
      <Footer />
    </div>
  );
}
