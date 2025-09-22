import { useLocation, Link } from 'react-router-dom';

export default function Confirm() {
  const { state } = useLocation();
  if (!state) return <p className="text-center mt-5">예약 정보가 없습니다.</p>;

  return (
    <div className="container text-center mt-5">
      <h2 style={{color:"#ff7f50"}}>예약이 완료되었습니다 🎉</h2>
      <div className="mt-4 p-4 border rounded bg-white shadow-sm d-inline-block">
        <p><strong>날짜:</strong> {state.date}</p>
        <p><strong>시간:</strong> {state.time}</p>
        <p><strong>인원:</strong> {state.people} 명</p>
      </div>
      <div className="mt-4">
        <Link to="/" className="btn btn-orange btn-lg">메인으로</Link>
      </div>
    </div>
  );
}
