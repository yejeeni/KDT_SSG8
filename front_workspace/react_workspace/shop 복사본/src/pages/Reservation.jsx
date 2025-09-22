import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import BackButton from '../components/BackButton'

export default function Reservation() {
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const [people, setPeople] = useState(2);
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    navigate('/confirm', { state: { date, time, people } });
  };

  return (
    <div className="container mt-5">
      <h2 className="text-center mb-4" style={{color:"#ff7f50"}}>예약하기</h2>
      <form onSubmit={handleSubmit} className="col-10 col-md-6 mx-auto p-4 border rounded bg-white shadow-sm">
      <BackButton/>
        <div className="mb-3">
          <label className="form-label">예약 날짜</label>
          <input type="date" className="form-control" value={date} onChange={e=>setDate(e.target.value)} required/>
        </div>
        <div className="mb-3">
          <label className="form-label">예약 시간</label>
          <input type="time" className="form-control" value={time} onChange={e=>setTime(e.target.value)} required/>
        </div>
        <div className="mb-3">
          <label className="form-label">인원 수</label>
          <input type="number" className="form-control" min="1" max="20"
                 value={people} onChange={e=>setPeople(e.target.value)} required/>
        </div>
        <button type="submit" className="btn btn-orange w-100 btn-lg">예약 완료</button>
      </form>
    </div>
  );
}
