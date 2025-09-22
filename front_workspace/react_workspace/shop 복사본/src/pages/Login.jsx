import { useState } from 'react';
import BackButton from '../components/BackButton';

export default function Login() {
  const [form, setForm] = useState({ username: '', password: '' });

  const handleChange = e => {
    setForm({...form, [e.target.name]: e.target.value});
  };

  const handleSubmit = e => {
    e.preventDefault();
    alert(`로그인 시도\n아이디: ${form.username}`);
    // 실제 서비스라면 API 호출
  };

  const loginRequest = (provider)=>{
    //원래는 각 provider별로 인증 url이 다르므로, 조건으로 처리해야 하지만 spring boot에서 지원하는 oath2 라이브러리를 사용중이므로, 이 url을 서버측 스프링이 알아서 처리해 줌
    window.location.href=`http://localhost:7777/memberapp/oauth2/authorization/${provider}`;
  }

  return (
    <div className="container mt-5 col-10 col-md-6">
      <BackButton/>
      <h2 className="text-center mb-4" style={{color:"#ff7f50"}}>로그인</h2>
      <form className="p-4 border rounded bg-white shadow-sm" onSubmit={handleSubmit}>
        <input className="form-control mb-3" name="username" placeholder="아이디"
               value={form.username} onChange={handleChange} required />
        <input type="password" className="form-control mb-4" name="password" placeholder="비밀번호"
               value={form.password} onChange={handleChange} required />
        <button type="submit" className="btn btn-orange w-100">로그인</button>

        <div className="text-center mb-2 fw-bold">또는 SNS로 로그인</div>
        <div className="d-grid gap-2">
          <button type="button" className="btn btn-success" onClick={()=>{
            loginRequest('naver')
          }}>Naver</button>
          <button type="button" className="btn btn-warning text-dark" onClick={()=>{
            loginRequest('kakao')
          }}>Kakao</button>
          <button type="button" className="btn btn-danger" onClick={()=>{
            loginRequest('google')
          }}>Google</button>
        </div>

      </form>
    </div>
  );
}
