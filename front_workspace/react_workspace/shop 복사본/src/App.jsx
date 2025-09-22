import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Reservation from './pages/Reservation';
import Confirm from './pages/Confirm';
import MenuList from './pages/MenuList';
import Signup from './pages/Signup';
import Login from './pages/Login';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home/>} />
        <Route path="/reserve" element={<Reservation/>} />
        <Route path="/confirm" element={<Confirm/>} />
        <Route path="/menu" element={<MenuList/>} /> 
        <Route path="/signup" element={<Signup/>} />
        <Route path="/login" element={<Login/>} />
      </Routes>
    </BrowserRouter>
  );
}
