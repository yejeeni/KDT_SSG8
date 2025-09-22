import { useState, useEffect, useRef } from 'react';
import BackButton from '../components/BackButton';

// 샘플 음식 데이터 (실제 서비스라면 API 요청)
const allMenus = Array.from({ length: 60 }).map((_, i) => ({
  id: i + 1,
  name: `메뉴 ${i + 1}`,
  price: `${(Math.random() * 20 + 5).toFixed(0)}000원`,
  img: `https://picsum.photos/seed/menu${i}/200/150`
}));

export default function MenuList() {
  const [visibleMenus, setVisibleMenus] = useState([]);
  const [hasMore, setHasMore] = useState(true);
  const loaderRef = useRef(null);
  const perLoad = 8; // 한 번에 추가 로드 수

  // 첫 로드
  useEffect(() => {
    loadMore();
  }, []);

  // Intersection Observer로 무한 스크롤
  useEffect(() => {
    const observer = new IntersectionObserver(entries => {
      if (entries[0].isIntersecting && hasMore) {
        loadMore();
      }
    }, { threshold: 1.0 });
    if (loaderRef.current) observer.observe(loaderRef.current);
    return () => observer.disconnect();
  }, [hasMore]);

  const loadMore = () => {
    const next = visibleMenus.length + perLoad;
    setVisibleMenus(allMenus.slice(0, next));
    if (next >= allMenus.length) setHasMore(false);
  };

  return (
    <div className="container mt-4">
      <BackButton/>
      <h2 className="text-center mb-4" style={{color:"#ff7f50"}}>🍴 음식 메뉴</h2>

      <div className="row g-4">
        {visibleMenus.map(menu => (
          <div className="col-6 col-md-3" key={menu.id}>
            <div className="card shadow-sm h-100">
              <img src={menu.img} className="card-img-top" alt={menu.name}/>
              <div className="card-body text-center">
                <h5 className="card-title">{menu.name}</h5>
                <p className="card-text text-muted">{menu.price}</p>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* 로딩 트리거 */}
      {hasMore && (
        <div ref={loaderRef} className="text-center py-4 text-secondary">
          ↓ 스크롤하면 더 보기
        </div>
      )}
    </div>
  );
}
