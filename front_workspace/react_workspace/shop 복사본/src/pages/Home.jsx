import { Link } from 'react-router-dom';

export default function Home() {
  return (
    <div className="container text-center py-5">
      <h1 className="mb-4" style={{color:"#ff7f50"}}>🍽️ 식당 키오스크</h1>
      <div className="d-grid gap-3 col-6 mx-auto">
        <Link to="/reserve" className="btn btn-orange btn-lg">예약하기</Link>
        <Link to="/menu" className="btn btn-light btn-lg border">음식 메뉴</Link>
        <Link to="/signup" className="btn btn-light btn-lg border">회원가입</Link>
        <Link to="/login" className="btn btn-light btn-lg border">로그인</Link>
      </div>
    </div>
  );
}
