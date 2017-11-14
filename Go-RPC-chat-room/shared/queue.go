package shared
//
//
//type MessageQueue struct {
//	Messages *[]*Message
//}
//
//func NewMessageQueue() *MessageQueue{
//	return &MessageQueue{Messages: &[]*Message{&Message{Client:"test", Message:"test",Room:"test"}}}
//}
//
//func (mq *MessageQueue) Push(m *Message) {
//	*mq.Messages = append(*(mq.Messages), m)
//}
//
//func (mq *MessageQueue) Top() *Message{
//	if len((*(mq.Messages))) == 0 {
//		return nil
//	}
//
//	return (*mq.Messages)[0]
//}
//
//func (mq *MessageQueue) Pop() *Message{
//	if len((*(mq.Messages))) == 0 {
//		return nil
//	}
//	m, q := (*(mq.Messages))[0], (*(mq.Messages))[1:]
//	*mq.Messages = q
//
//	return m
//}
//
//
