import { useNavigate } from 'react-router-dom';

export default function BackButton() {
  const navigate = useNavigate();
  return (
    <button
      className="btn btn-light border mb-3"
      onClick={() => navigate(-1)}
    >
      ← 뒤로가기
    </button>
  );
}
