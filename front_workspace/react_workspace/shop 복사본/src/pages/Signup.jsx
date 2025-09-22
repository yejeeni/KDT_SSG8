import { useState } from 'react';
import BackButton from '../components/BackButton';

export default function Signup() {
  const [form, setForm] = useState({
    username: '',
    password: '',
    email: '',
    name: ''
  });

  const handleChange = e => {
    setForm({...form, [e.target.name]: e.target.value});
  };

  const handleSubmit = e => {
    e.preventDefault();
    alert(`회원가입 완료\n아이디: ${form.username}`);
    // 실제 서비스라면 API 호출
  };

  return (
    <div className="container mt-5 col-10 col-md-6">
      <BackButton/>
      <h2 className="text-center mb-4" style={{color:"#ff7f50"}}>회원가입</h2>
      <form className="p-4 border rounded bg-white shadow-sm" onSubmit={handleSubmit}>
        <input className="form-control mb-3" name="username" placeholder="아이디"
               value={form.username} onChange={handleChange} required />
        <input type="password" className="form-control mb-3" name="password" placeholder="비밀번호"
               value={form.password} onChange={handleChange} required />
        <input type="email" className="form-control mb-3" name="email" placeholder="이메일"
               value={form.email} onChange={handleChange} required />
        <input className="form-control mb-4" name="name" placeholder="이름"
               value={form.name} onChange={handleChange} required />
        <button type="submit" className="btn btn-orange w-100 mb-4">일반 회원가입</button>

      </form>
    </div>
  );
}
